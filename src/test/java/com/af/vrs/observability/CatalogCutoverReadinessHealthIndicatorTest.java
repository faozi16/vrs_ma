package com.af.vrs.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class CatalogCutoverReadinessHealthIndicatorTest {

    @Test
    void shouldReportUnknownWhenInsufficientSamples() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CatalogCutoverGateProperties properties = new CatalogCutoverGateProperties();
        properties.setMinObservedCalls(5);

        meterRegistry.counter("catalog.facade.remote.calls", "result", "success").increment(2);

        CatalogCutoverReadinessHealthIndicator indicator =
                new CatalogCutoverReadinessHealthIndicator(meterRegistry, properties);

        assertEquals(Status.UNKNOWN, indicator.health().getStatus());
    }

    @Test
    void shouldReportUpWhenAllGatesPass() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CatalogCutoverGateProperties properties = new CatalogCutoverGateProperties();
        properties.setMinObservedCalls(1);
        properties.setMinSuccessRatio(0.5);
        properties.setMaxFailureRatio(0.5);
        properties.setMaxP95LatencyMs(1000);
        properties.setMaxCircuitOpenCount(10);
        properties.setMaxFallbackHitCount(10);

        meterRegistry.counter("catalog.facade.remote.calls", "result", "success").increment(9);
        meterRegistry.counter("catalog.facade.remote.calls", "result", "failure").increment(1);
        meterRegistry.timer("catalog.facade.remote.latency").record(50, TimeUnit.MILLISECONDS);

        CatalogCutoverReadinessHealthIndicator indicator =
                new CatalogCutoverReadinessHealthIndicator(meterRegistry, properties);

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void shouldReportDownWhenSuccessRatioBreachesGate() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CatalogCutoverGateProperties properties = new CatalogCutoverGateProperties();
        properties.setMinObservedCalls(1);
        properties.setMinSuccessRatio(0.9);
        properties.setMaxFailureRatio(0.2);
        properties.setMaxP95LatencyMs(1000);

        meterRegistry.counter("catalog.facade.remote.calls", "result", "success").increment(2);
        meterRegistry.counter("catalog.facade.remote.calls", "result", "failure").increment(3);
        meterRegistry.timer("catalog.facade.remote.latency").record(25, TimeUnit.MILLISECONDS);

        CatalogCutoverReadinessHealthIndicator indicator =
                new CatalogCutoverReadinessHealthIndicator(meterRegistry, properties);

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
