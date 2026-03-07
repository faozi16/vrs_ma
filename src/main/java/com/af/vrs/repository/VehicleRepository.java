package com.af.vrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
