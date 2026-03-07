package com.af.vrs.billing.application;

import java.util.List;

import com.af.vrs.entity.Payment;

public interface PaymentApplicationService {
    Payment savePayment(Payment payment);

    List<Payment> getAllPayments();

    Payment getPaymentById(Long paymentId);

    Payment updatePayment(Long paymentId, Payment paymentDetails);

    void deletePayment(Long paymentId);
}
