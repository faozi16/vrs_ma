package com.af.vrs.shared.facade;

import java.util.Optional;

public interface BillingReadFacade {
    Optional<String> findPaymentStatusById(Long paymentId);

    boolean hasPaymentForReservation(Long reservationId);
}
