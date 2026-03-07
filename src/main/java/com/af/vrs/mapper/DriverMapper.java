package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.DriverDto;
import com.af.vrs.entity.Driver;

@Mapper(
    componentModel = "spring"
)
public interface DriverMapper {
    DriverDto driverToDriverDto(Driver driver);
    Driver driverDtoToDriver(DriverDto driverDto);
}
