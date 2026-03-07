package com.af.vrs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	boolean existsByReservationId(Long reservationId);

	Optional<Payment> findByReservationId(Long reservationId);
}
