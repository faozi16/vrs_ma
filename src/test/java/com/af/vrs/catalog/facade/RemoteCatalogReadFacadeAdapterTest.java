package com.af.vrs.catalog.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.af.vrs.shared.facade.model.VehicleSummary;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class RemoteCatalogReadFacadeAdapterTest {

    @Test
    void shouldMapVehicleResponseFromRemoteCatalog() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(ExpectedCount.times(2), requestTo("http://localhost:18002/api/vehicles/7"))
                .andRespond(withSuccess("{\"vehicleId\":7,\"vehicleType\":\"SUV\",\"status\":\"AVAILABLE\"}",
                        MediaType.APPLICATION_JSON));

        RestClient restClient = builder
                .baseUrl("http://localhost:18002")
                .build();

        RemoteCatalogReadFacadeAdapter adapter = new RemoteCatalogReadFacadeAdapter(restClient, 1, 0);

        Optional<VehicleSummary> result = adapter.findVehicle(7L);

        assertTrue(result.isPresent());
        assertEquals(7L, result.get().vehicleId());
        assertEquals("SUV", result.get().vehicleType());
        assertTrue(adapter.isVehicleAvailable(7L));

        server.verify();
    }

    @Test
    void shouldRetryThenReturnEmptyWhenRemoteFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(ExpectedCount.times(2), requestTo("http://localhost:18002/api/vehicles/9"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        RestClient restClient = builder
                .baseUrl("http://localhost:18002")
                .build();

        RemoteCatalogReadFacadeAdapter adapter = new RemoteCatalogReadFacadeAdapter(restClient, 2, 0);

        Optional<VehicleSummary> result = adapter.findVehicle(9L);

        assertFalse(result.isPresent());

        server.verify();
    }

        @Test
        void shouldUseLocalFallbackAndOpenCircuitAfterFailureThreshold() {
                RestClient.Builder builder = RestClient.builder();
                MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

                server.expect(ExpectedCount.times(1), requestTo("http://localhost:18002/api/vehicles/11"))
                                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

                RestClient restClient = builder
                                .baseUrl("http://localhost:18002")
                                .build();

                LocalCatalogFallbackReadService fallbackReadService = org.mockito.Mockito.mock(LocalCatalogFallbackReadService.class);
                when(fallbackReadService.findVehicle(11L)).thenReturn(Optional.of(new VehicleSummary(11L, "SUV", "AVAILABLE")));

                RemoteCatalogReadFacadeAdapter adapter = new RemoteCatalogReadFacadeAdapter(
                                restClient,
                                1,
                                0,
                                1,
                                60000,
                                true,
                                fallbackReadService,
                                new SimpleMeterRegistry());

                Optional<VehicleSummary> firstAttempt = adapter.findVehicle(11L);
                Optional<VehicleSummary> secondAttempt = adapter.findVehicle(11L);

                assertTrue(firstAttempt.isPresent());
                assertTrue(secondAttempt.isPresent());
                assertTrue(adapter.isVehicleAvailable(11L));

                server.verify();
        }
}
