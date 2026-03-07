package com.af.vrs.catalog.facade;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.af.vrs.repository.VehicleRepository;
import com.af.vrs.shared.facade.CatalogReadFacade;
import com.af.vrs.shared.facade.model.VehicleSummary;

@Service
public class CatalogReadFacadeAdapter implements CatalogReadFacade {
    private final VehicleRepository vehicleRepository;

    public CatalogReadFacadeAdapter(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Optional<VehicleSummary> findVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> new VehicleSummary(vehicle.getVehicleId(), vehicle.getVehicleType(), vehicle.getStatus()));
    }

    @Override
    public boolean isVehicleAvailable(Long vehicleId) {
        return findVehicle(vehicleId)
                .map(vehicle -> "AVAILABLE".equalsIgnoreCase(vehicle.status()))
                .orElse(false);
    }
}
