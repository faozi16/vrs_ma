package com.af.vrs.catalog.facade;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.af.vrs.repository.VehicleRepository;
import com.af.vrs.shared.facade.model.VehicleSummary;

@Service
public class LocalCatalogFallbackReadService {
    private final VehicleRepository vehicleRepository;

    public LocalCatalogFallbackReadService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Optional<VehicleSummary> findVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> new VehicleSummary(vehicle.getVehicleId(), vehicle.getVehicleType(), vehicle.getStatus()));
    }
}
