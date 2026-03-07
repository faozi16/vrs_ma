package com.af.vrs.reservation.application;

public record ReferenceValidationResult(boolean customerExists, boolean vehicleExists, boolean vehicleAvailable) {
    public boolean isValidForReservationCreate() {
        return customerExists && vehicleExists && vehicleAvailable;
    }
}
