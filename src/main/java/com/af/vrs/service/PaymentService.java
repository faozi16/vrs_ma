package com.af.vrs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.af.vrs.billing.application.PaymentApplicationService;
import com.af.vrs.entity.Payment;
import com.af.vrs.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PaymentService implements PaymentApplicationService {
    @Autowired
    private PaymentRepository paymentRepository;

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }

    public Payment updatePayment(Long paymentId, Payment paymentDetails) {
        Payment payment = getPaymentById(paymentId);
        payment.setReservationId(paymentDetails.getReservationId());
        payment.setPaymentTime(paymentDetails.getPaymentTime());
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentMethod(paymentDetails.getPaymentMethod());
        payment.setStatus(paymentDetails.getStatus());
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        paymentRepository.delete(payment);
    }
}
