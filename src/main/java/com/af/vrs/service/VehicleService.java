package com.af.vrs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.af.vrs.catalog.application.VehicleApplicationService;
import com.af.vrs.entity.Vehicle;
import com.af.vrs.repository.VehicleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VehicleService implements VehicleApplicationService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId).orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
    }

    public Vehicle updateVehicle(Long vehicleId, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicle.setDriverId(vehicleDetails.getDriverId());
        vehicle.setVehicleType(vehicleDetails.getVehicleType());
        vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        vehicle.setStatus(vehicleDetails.getStatus());
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicleRepository.delete(vehicle);
    }
}
