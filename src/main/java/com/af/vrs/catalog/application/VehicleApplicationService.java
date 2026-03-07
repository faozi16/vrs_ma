package com.af.vrs.catalog.application;

import java.util.List;

import com.af.vrs.entity.Vehicle;

public interface VehicleApplicationService {
    Vehicle saveVehicle(Vehicle vehicle);

    List<Vehicle> getAllVehicles();

    Vehicle getVehicleById(Long vehicleId);

    Vehicle updateVehicle(Long vehicleId, Vehicle vehicleDetails);

    void deleteVehicle(Long vehicleId);
}
