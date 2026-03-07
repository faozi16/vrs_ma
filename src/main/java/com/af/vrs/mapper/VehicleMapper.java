package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.VehicleDto;
import com.af.vrs.entity.Vehicle;

@Mapper(
    componentModel = "spring"
)
public interface VehicleMapper {
    VehicleDto vehicleToVehicleDto(Vehicle vehicle);
    Vehicle vehicleDtoToVehicle(VehicleDto vehicleDto);
}
