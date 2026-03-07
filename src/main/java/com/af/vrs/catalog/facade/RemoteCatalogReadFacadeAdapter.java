package com.af.vrs.catalog.facade;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.af.vrs.catalog.facade.remote.CatalogVehicleResponse;
import com.af.vrs.shared.facade.CatalogReadFacade;
import com.af.vrs.shared.facade.model.VehicleSummary;

@Service
@ConditionalOnProperty(name = "app.catalog.read.mode", havingValue = "remote")
public class RemoteCatalogReadFacadeAdapter implements CatalogReadFacade {
    private final RestClient restClient;

    public RemoteCatalogReadFacadeAdapter(RestClient.Builder builder,
            @Value("${app.catalog.remote.base-url}") String catalogBaseUrl) {
        this.restClient = builder.baseUrl(catalogBaseUrl).build();
    }

    @Override
    public Optional<VehicleSummary> findVehicle(Long vehicleId) {
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
            return Optional.empty();
        }
    }

    @Override
    public boolean isVehicleAvailable(Long vehicleId) {
        return findVehicle(vehicleId)
                .map(vehicle -> "AVAILABLE".equalsIgnoreCase(vehicle.status()))
                .orElse(false);
    }
}
