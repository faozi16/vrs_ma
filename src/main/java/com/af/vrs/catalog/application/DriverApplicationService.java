package com.af.vrs.catalog.application;

import java.util.List;

import com.af.vrs.entity.Driver;

public interface DriverApplicationService {
    Driver saveDriver(Driver driver);

    List<Driver> getAllDrivers();

    Driver getDriverById(Long driverId);

    Driver updateDriver(Long driverId, Driver driverDetails);

    void deleteDriver(Long driverId);
}
