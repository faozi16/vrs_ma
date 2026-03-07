package com.af.vrs.shared.facade.model;

public record ReservationSummary(Long reservationId, Long customerId, Long vehicleId, String status) {
}
