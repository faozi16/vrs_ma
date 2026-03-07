package com.af.vrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.af.vrs.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
