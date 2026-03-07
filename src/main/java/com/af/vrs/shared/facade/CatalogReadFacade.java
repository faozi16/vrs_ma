package com.af.vrs.shared.facade;

import java.util.Optional;

import com.af.vrs.shared.facade.model.VehicleSummary;

public interface CatalogReadFacade {
    Optional<VehicleSummary> findVehicle(Long vehicleId);

    boolean isVehicleAvailable(Long vehicleId);
}
