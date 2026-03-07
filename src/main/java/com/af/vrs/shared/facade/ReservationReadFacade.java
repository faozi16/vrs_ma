package com.af.vrs.shared.facade;

import java.util.Optional;

import com.af.vrs.shared.facade.model.ReservationSummary;

public interface ReservationReadFacade {
    Optional<ReservationSummary> findReservation(Long reservationId);
}
