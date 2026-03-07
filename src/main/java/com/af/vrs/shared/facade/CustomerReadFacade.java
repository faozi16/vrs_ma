package com.af.vrs.shared.facade;

import java.util.Optional;

import com.af.vrs.shared.facade.model.CustomerSummary;

public interface CustomerReadFacade {
    Optional<CustomerSummary> findCustomer(Long customerId);

    boolean exists(Long customerId);
}
