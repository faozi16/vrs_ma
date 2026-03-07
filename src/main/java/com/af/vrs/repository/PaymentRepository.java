package com.af.vrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
