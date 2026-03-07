package com.af.vrs.catalog.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.af.vrs.entity.Vehicle;
import com.af.vrs.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class CatalogReadFacadeAdapterTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Test
    void shouldReturnVehicleSummaryFromLocalRepository() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(7L);
        vehicle.setVehicleType("SUV");
        vehicle.setStatus("AVAILABLE");

        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(vehicle));

        CatalogReadFacadeAdapter adapter = new CatalogReadFacadeAdapter(vehicleRepository);

        Optional<com.af.vrs.shared.facade.model.VehicleSummary> summary = adapter.findVehicle(7L);

        assertTrue(summary.isPresent());
        assertEquals(7L, summary.get().vehicleId());
        assertEquals("SUV", summary.get().vehicleType());
        assertTrue(adapter.isVehicleAvailable(7L));
    }

    @Test
    void shouldReturnUnavailableWhenVehicleMissing() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        CatalogReadFacadeAdapter adapter = new CatalogReadFacadeAdapter(vehicleRepository);

        assertFalse(adapter.findVehicle(99L).isPresent());
        assertFalse(adapter.isVehicleAvailable(99L));
    }
}
