package com.af.vrs.reservation.application;

import java.util.List;

import com.af.vrs.entity.Reservation;

public interface ReservationApplicationService {
    Reservation saveReservation(Reservation reservation);

    List<Reservation> getAllReservations();

    Reservation getReservationById(Long reservationId);

    Reservation updateReservation(Long reservationId, Reservation reservationDetails);

    void deleteReservation(Long reservationId);
}
