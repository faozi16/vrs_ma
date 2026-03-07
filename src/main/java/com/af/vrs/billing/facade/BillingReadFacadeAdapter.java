package com.af.vrs.billing.facade;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.af.vrs.repository.PaymentRepository;
import com.af.vrs.shared.facade.BillingReadFacade;

@Service
public class BillingReadFacadeAdapter implements BillingReadFacade {
    private final PaymentRepository paymentRepository;

    public BillingReadFacadeAdapter(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Optional<String> findPaymentStatusById(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> payment.getStatus());
    }

    @Override
    public boolean hasPaymentForReservation(Long reservationId) {
        return paymentRepository.existsByReservationId(reservationId);
    }
}
