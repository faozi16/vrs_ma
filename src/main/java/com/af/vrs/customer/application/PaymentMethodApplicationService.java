package com.af.vrs.customer.application;

import java.util.List;

import com.af.vrs.entity.PaymentMethod;

public interface PaymentMethodApplicationService {
    PaymentMethod savePaymentMethod(PaymentMethod paymentMethod);

    List<PaymentMethod> getAllPaymentMethods();

    List<PaymentMethod> getByCustomerId(Long customerId);

    PaymentMethod getPaymentMethodById(Long id);

    PaymentMethod updatePaymentMethod(Long id, PaymentMethod paymentMethodDetails);

    void deletePaymentMethod(Long id);
}
