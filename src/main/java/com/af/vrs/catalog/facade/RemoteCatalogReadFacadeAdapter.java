package com.af.vrs.catalog.facade;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.af.vrs.catalog.facade.remote.CatalogVehicleResponse;
import com.af.vrs.shared.facade.CatalogReadFacade;
import com.af.vrs.shared.facade.model.VehicleSummary;

@Service
@ConditionalOnProperty(name = "app.catalog.read.mode", havingValue = "remote")
public class RemoteCatalogReadFacadeAdapter implements CatalogReadFacade {
    private final RestClient restClient;
    private final int maxAttempts;
    private final long backoffMillis;

    public RemoteCatalogReadFacadeAdapter(RestClient.Builder builder,
            @Value("${app.catalog.remote.base-url}") String catalogBaseUrl,
            @Value("${app.catalog.remote.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${app.catalog.remote.read-timeout-ms:3000}") int readTimeoutMs,
            @Value("${app.catalog.remote.max-attempts:2}") int maxAttempts,
            @Value("${app.catalog.remote.retry-backoff-ms:100}") long backoffMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = builder
                .baseUrl(catalogBaseUrl)
                .requestFactory(requestFactory)
                .build();
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMillis = Math.max(0L, backoffMillis);
    }

    RemoteCatalogReadFacadeAdapter(RestClient restClient, int maxAttempts, long backoffMillis) {
        this.restClient = restClient;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMillis = Math.max(0L, backoffMillis);
    }

    @Override
    public Optional<VehicleSummary> findVehicle(Long vehicleId) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                CatalogVehicleResponse response = restClient.get()
                        .uri("/api/vehicles/{id}", vehicleId)
                        .retrieve()
                        .body(CatalogVehicleResponse.class);

                if (response == null || response.getVehicleId() == null) {
                    return Optional.empty();
                }

                return Optional.of(new VehicleSummary(response.getVehicleId(), response.getVehicleType(), response.getStatus()));
            } catch (RestClientException ex) {
                if (attempt == maxAttempts) {
                    return Optional.empty();
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
}
