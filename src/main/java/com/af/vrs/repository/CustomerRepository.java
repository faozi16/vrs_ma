package com.af.vrs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Optional<Customer> findByUsername(String username);
}
