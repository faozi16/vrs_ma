package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.PaymentMethodDto;
import com.af.vrs.entity.PaymentMethod;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {
    PaymentMethodDto paymentMethodToPaymentMethodDto(PaymentMethod pm);
    PaymentMethod paymentMethodDtoToPaymentMethod(PaymentMethodDto pmDto);
}
