package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.PaymentDto;
import com.af.vrs.entity.Payment;

@Mapper(
    componentModel = "spring"
)
public interface PaymentMapper {
    PaymentDto paymentToPaymentDto(Payment payment);
    Payment paymentDtoToPayment(PaymentDto paymentDto);
}
