package com.af.vrs.customer.facade;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.af.vrs.repository.CustomerRepository;
import com.af.vrs.shared.facade.CustomerReadFacade;
import com.af.vrs.shared.facade.model.CustomerSummary;

@Service
public class CustomerReadFacadeAdapter implements CustomerReadFacade {
    private final CustomerRepository customerRepository;

    public CustomerReadFacadeAdapter(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Optional<CustomerSummary> findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .map(customer -> new CustomerSummary(customer.getCustomerId(), customer.getStatus()));
    }

    @Override
    public boolean exists(Long customerId) {
        return customerRepository.existsById(customerId);
    }
}
