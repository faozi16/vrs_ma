package com.af.vrs.catalog.facade;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.af.vrs.catalog.facade.remote.CatalogVehicleResponse;
import com.af.vrs.shared.facade.CatalogReadFacade;
import com.af.vrs.shared.facade.model.VehicleSummary;

import io.micrometer.core.instrument.MeterRegistry;

@Service
@ConditionalOnProperty(name = "app.catalog.read.mode", havingValue = "remote")
public class RemoteCatalogReadFacadeAdapter implements CatalogReadFacade {
    private final RestClient restClient;
    private final int maxAttempts;
    private final long backoffMillis;
    private final String vehicleByIdPath;
    private final int circuitFailureThreshold;
    private final long circuitOpenMillis;
    private final boolean fallbackToLocal;
    private final LocalCatalogFallbackReadService localFallbackReadService;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong circuitOpenUntilEpochMs = new AtomicLong(0L);

    @Autowired
    public RemoteCatalogReadFacadeAdapter(RestClient.Builder builder,
            @Value("${app.catalog.remote.base-url}") String catalogBaseUrl,
            @Value("${app.catalog.remote.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${app.catalog.remote.read-timeout-ms:3000}") int readTimeoutMs,
            @Value("${app.catalog.remote.max-attempts:2}") int maxAttempts,
            @Value("${app.catalog.remote.retry-backoff-ms:100}") long backoffMillis,
            @Value("${app.catalog.remote.vehicle-by-id-path:/api/vehicles/{id}}") String vehicleByIdPath,
            @Value("${app.catalog.remote.circuit.failure-threshold:3}") int circuitFailureThreshold,
            @Value("${app.catalog.remote.circuit.open-ms:5000}") long circuitOpenMillis,
            @Value("${app.catalog.remote.fallback-to-local:true}") boolean fallbackToLocal,
            ObjectProvider<LocalCatalogFallbackReadService> localFallbackReadServiceProvider,
            ObjectProvider<MeterRegistry> meterRegistryProvider) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = builder
                .baseUrl(catalogBaseUrl)
                .requestFactory(requestFactory)
                .build();
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMillis = Math.max(0L, backoffMillis);
        this.vehicleByIdPath = vehicleByIdPath;
        this.circuitFailureThreshold = Math.max(1, circuitFailureThreshold);
        this.circuitOpenMillis = Math.max(1L, circuitOpenMillis);
        this.fallbackToLocal = fallbackToLocal;
        this.localFallbackReadService = localFallbackReadServiceProvider.getIfAvailable();
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
    }

    RemoteCatalogReadFacadeAdapter(RestClient restClient, int maxAttempts, long backoffMillis) {
        this(restClient, maxAttempts, backoffMillis, "/api/vehicles/{id}", 3, 5000L, true, null, null);
    }

    RemoteCatalogReadFacadeAdapter(
            RestClient restClient,
            int maxAttempts,
            long backoffMillis,
            String vehicleByIdPath,
            int circuitFailureThreshold,
            long circuitOpenMillis,
            boolean fallbackToLocal,
            LocalCatalogFallbackReadService localFallbackReadService,
            MeterRegistry meterRegistry) {
        this.restClient = restClient;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMillis = Math.max(0L, backoffMillis);
        this.vehicleByIdPath = vehicleByIdPath;
        this.circuitFailureThreshold = Math.max(1, circuitFailureThreshold);
        this.circuitOpenMillis = Math.max(1L, circuitOpenMillis);
        this.fallbackToLocal = fallbackToLocal;
        this.localFallbackReadService = localFallbackReadService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Optional<VehicleSummary> findVehicle(Long vehicleId) {
        if (isCircuitOpen()) {
            recordCounter("catalog.facade.remote.calls", "result", "short_circuit");
            return fallbackOrEmpty(vehicleId, "circuit_open");
        }

        long startNs = System.nanoTime();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                CatalogVehicleResponse response = restClient.get()
                    .uri(vehicleByIdPath, vehicleId)
                        .retrieve()
                        .body(CatalogVehicleResponse.class);

                if (response == null || response.getVehicleId() == null) {
                    recordCounter("catalog.facade.remote.calls", "result", "empty");
                    return Optional.empty();
                }

                onRemoteSuccess(startNs);
                return Optional.of(new VehicleSummary(response.getVehicleId(), response.getVehicleType(), response.getStatus()));
            } catch (RestClientException ex) {
                if (attempt == maxAttempts) {
                    onRemoteFailure(startNs);
                    return fallbackOrEmpty(vehicleId, "remote_error");
                }
                sleepBackoff();
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isVehicleAvailable(Long vehicleId) {
        return findVehicle(vehicleId)
                .map(vehicle -> "AVAILABLE".equalsIgnoreCase(vehicle.status()))
                .orElse(false);
    }

    private void sleepBackoff() {
        if (backoffMillis <= 0L) {
            return;
        }

        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private void onRemoteSuccess(long startNs) {
        consecutiveFailures.set(0);
        circuitOpenUntilEpochMs.set(0L);
        recordDuration(startNs);
        recordCounter("catalog.facade.remote.calls", "result", "success");
    }

    private void onRemoteFailure(long startNs) {
        recordDuration(startNs);
        recordCounter("catalog.facade.remote.calls", "result", "failure");
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= circuitFailureThreshold) {
            circuitOpenUntilEpochMs.set(System.currentTimeMillis() + circuitOpenMillis);
            recordCounter("catalog.facade.remote.circuit", "state", "opened");
        }
    }

    private boolean isCircuitOpen() {
        long openUntil = circuitOpenUntilEpochMs.get();
        if (openUntil == 0L) {
            return false;
        }
        boolean open = System.currentTimeMillis() < openUntil;
        if (!open) {
            circuitOpenUntilEpochMs.set(0L);
            consecutiveFailures.set(0);
            recordCounter("catalog.facade.remote.circuit", "state", "closed");
        }
        return open;
    }

    private Optional<VehicleSummary> fallbackOrEmpty(Long vehicleId, String reason) {
        if (!fallbackToLocal || localFallbackReadService == null) {
            recordCounter("catalog.facade.remote.fallback", "result", "disabled");
            return Optional.empty();
        }

        Optional<VehicleSummary> fallback = localFallbackReadService.findVehicle(vehicleId);
        recordCounter("catalog.facade.remote.fallback", "result", fallback.isPresent() ? "hit" : "miss");
        recordCounter("catalog.facade.remote.fallback", "reason", reason);
        return fallback;
    }

    private void recordDuration(long startNs) {
        if (meterRegistry == null) {
            return;
        }
        meterRegistry.timer("catalog.facade.remote.latency").record(System.nanoTime() - startNs,
                java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    private void recordCounter(String name, String tagKey, String tagValue) {
        if (meterRegistry == null) {
            return;
        }
        meterRegistry.counter(name, tagKey, tagValue).increment();
    }
}
