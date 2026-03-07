package com.af.vrs.customer.application;

import java.util.List;

import com.af.vrs.entity.Customer;

public interface CustomerApplicationService {
    Customer saveCustomer(Customer customer);

    List<Customer> getAllCustomers();

    Customer getCustomerById(Long customerId);

    Customer updateCustomer(Long customerId, Customer customerDetails);

    void deleteCustomer(Long customerId);
}
