package com.af.vrs.mapper;

import org.mapstruct.Mapper;

import com.af.vrs.dto.ReservationDto;
import com.af.vrs.entity.Reservation;

@Mapper(
    componentModel = "spring"
)
public interface ReservationMapper {
    ReservationDto reservationToReservationDto(Reservation reservation);
    Reservation reservationDtoToReservation(ReservationDto reservationDto);
}
