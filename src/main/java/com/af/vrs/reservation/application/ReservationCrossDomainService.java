package com.af.vrs.reservation.application;

import org.springframework.stereotype.Service;

import com.af.vrs.shared.facade.CatalogReadFacade;
import com.af.vrs.shared.facade.CustomerReadFacade;

@Service
public class ReservationCrossDomainService {
    private final CustomerReadFacade customerReadFacade;
    private final CatalogReadFacade catalogReadFacade;

    public ReservationCrossDomainService(CustomerReadFacade customerReadFacade, CatalogReadFacade catalogReadFacade) {
        this.customerReadFacade = customerReadFacade;
        this.catalogReadFacade = catalogReadFacade;
    }

    public ReferenceValidationResult validateReservationReferences(Long customerId, Long vehicleId) {
        boolean customerExists = customerReadFacade.exists(customerId);
        boolean vehicleExists = catalogReadFacade.findVehicle(vehicleId).isPresent();
        boolean vehicleAvailable = catalogReadFacade.isVehicleAvailable(vehicleId);
        return new ReferenceValidationResult(customerExists, vehicleExists, vehicleAvailable);
    }
}
