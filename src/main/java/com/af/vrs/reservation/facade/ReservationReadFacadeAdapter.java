package com.af.vrs.reservation.facade;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.af.vrs.repository.ReservationRepository;
import com.af.vrs.shared.facade.ReservationReadFacade;
import com.af.vrs.shared.facade.model.ReservationSummary;

@Service
public class ReservationReadFacadeAdapter implements ReservationReadFacade {
    private final ReservationRepository reservationRepository;

    public ReservationReadFacadeAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Optional<ReservationSummary> findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> new ReservationSummary(
                        reservation.getReservationId(),
                        reservation.getCustomerId(),
                        reservation.getVehicleId(),
                        reservation.getStatus()));
    }
}
