package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.CustomerDto;
import com.af.vrs.entity.Customer;

@Mapper(
    componentModel = "spring"
)
public interface CustomerMapper {
    CustomerDto customerToCustomerDto(Customer customer);
    Customer customerDtoToCustomer(CustomerDto customerDto);
}
