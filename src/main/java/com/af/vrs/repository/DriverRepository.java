package com.af.vrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}
