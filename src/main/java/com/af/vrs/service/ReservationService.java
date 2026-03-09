package com.af.vrs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.af.vrs.entity.Reservation;
import com.af.vrs.reservation.application.ReservationApplicationService;
import com.af.vrs.reservation.application.ReferenceValidationResult;
import com.af.vrs.reservation.application.ReservationCrossDomainService;
import com.af.vrs.repository.ReservationRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReservationService implements ReservationApplicationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationCrossDomainService reservationCrossDomainService;

    public Reservation saveReservation(Reservation reservation) {
        ReferenceValidationResult validation = reservationCrossDomainService.validateReservationReferences(
                reservation.getCustomerId(),
                reservation.getVehicleId());

        if (!validation.customerExists()) {
            throw new EntityNotFoundException("Customer not found");
        }
        if (!validation.vehicleExists()) {
            throw new EntityNotFoundException("Vehicle not found");
        }
        if (!validation.vehicleAvailable()) {
            throw new IllegalStateException("Vehicle is not available");
        }

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
    }

    public Reservation updateReservation(Long reservationId, Reservation reservationDetails) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setCustomerId(reservationDetails.getCustomerId());
        reservation.setVehicleId(reservationDetails.getVehicleId());
        reservation.setPickupTime(reservationDetails.getPickupTime());
        reservation.setPickupLocation(reservationDetails.getPickupLocation());
        reservation.setDropoffLocation(reservationDetails.getDropoffLocation());
        reservation.setStatus(reservationDetails.getStatus());
        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservationRepository.delete(reservation);
    }
}
