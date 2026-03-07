package com.af.vrs.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.af.vrs.dto.DriverDto;
import com.af.vrs.entity.Driver;
import com.af.vrs.mapper.DriverMapper;
import com.af.vrs.catalog.application.DriverApplicationService;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    @Autowired
    private DriverApplicationService driverService;

    @Autowired
    private DriverMapper driverMapper;

    @PostMapping("/create")
    public ResponseEntity<DriverDto> createDriver(@jakarta.validation.Valid @RequestBody DriverDto driverDto) {
        Driver entity = driverMapper.driverDtoToDriver(driverDto);
        Driver saved = driverService.saveDriver(entity);
        return ResponseEntity.ok(driverMapper.driverToDriverDto(saved));
    }

    @GetMapping("/get")
    public ResponseEntity<List<DriverDto>> getAllDrivers() {
        List<Driver> list = driverService.getAllDrivers();
        List<DriverDto> dtos = list.stream().map(driverMapper::driverToDriverDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DriverDto> getDriverById(@PathVariable Long id) {
        Driver d = driverService.getDriverById(id);
        return ResponseEntity.ok(driverMapper.driverToDriverDto(d));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverDto> updateDriver(@PathVariable Long id, @jakarta.validation.Valid @RequestBody DriverDto driverDetails) {
        Driver entity = driverMapper.driverDtoToDriver(driverDetails);
        Driver updated = driverService.updateDriver(id, entity);
        return ResponseEntity.ok(driverMapper.driverToDriverDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}
