package com.af.vrs.observability;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;

@Component("catalogCutoverReadiness")
@ConditionalOnProperty(name = "app.catalog.read.mode", havingValue = "remote")
public class CatalogCutoverReadinessHealthIndicator implements HealthIndicator {
    private final MeterRegistry meterRegistry;
    private final CatalogCutoverGateProperties properties;

    public CatalogCutoverReadinessHealthIndicator(MeterRegistry meterRegistry, CatalogCutoverGateProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (!properties.isEnabled()) {
            return Health.up().withDetail("gates.enabled", false).build();
        }

        double success = counterValue("catalog.facade.remote.calls", "result", "success");
        double failure = counterValue("catalog.facade.remote.calls", "result", "failure");
        double empty = counterValue("catalog.facade.remote.calls", "result", "empty");
        double shortCircuit = counterValue("catalog.facade.remote.calls", "result", "short_circuit");
        double fallbackHits = counterValue("catalog.facade.remote.fallback", "result", "hit");
        double circuitOpens = counterValue("catalog.facade.remote.circuit", "state", "opened");

        double total = success + failure + empty + shortCircuit;
        if (total < properties.getMinObservedCalls()) {
            return Health.unknown()
                    .withDetail("gates.enabled", true)
                    .withDetail("reason", "insufficient_data")
                    .withDetail("minObservedCalls", properties.getMinObservedCalls())
                    .withDetail("observedCalls", total)
                    .build();
        }

        double successRatio = safeRatio(success, total);
        double failureRatio = safeRatio(failure + shortCircuit, total);
        double p95LatencyMs = resolveP95LatencyMs();

        List<String> breaches = new ArrayList<>();
        if (successRatio < properties.getMinSuccessRatio()) {
            breaches.add("success_ratio");
        }
        if (failureRatio > properties.getMaxFailureRatio()) {
            breaches.add("failure_ratio");
        }
        if (p95LatencyMs > properties.getMaxP95LatencyMs()) {
            breaches.add("p95_latency_ms");
        }
        if (circuitOpens > properties.getMaxCircuitOpenCount()) {
            breaches.add("circuit_open_count");
        }
        if (fallbackHits > properties.getMaxFallbackHitCount()) {
            breaches.add("fallback_hit_count");
        }

        Health.Builder healthBuilder = breaches.isEmpty() ? Health.up() : Health.down();
        return healthBuilder
                .withDetail("gates.enabled", true)
                .withDetail("successRatio", successRatio)
                .withDetail("failureRatio", failureRatio)
                .withDetail("shortCircuitCount", shortCircuit)
                .withDetail("p95LatencyMs", p95LatencyMs)
                .withDetail("circuitOpenCount", circuitOpens)
                .withDetail("fallbackHitCount", fallbackHits)
                .withDetail("rollbackSuggested", !breaches.isEmpty())
                .withDetail("breaches", breaches)
                .build();
    }

    private double counterValue(String name, String tagKey, String tagValue) {
        Counter counter = meterRegistry.find(name).tag(tagKey, tagValue).counter();
        return counter == null ? 0d : counter.count();
    }

    private double resolveP95LatencyMs() {
        Timer timer = meterRegistry.find("catalog.facade.remote.latency").timer();
        if (timer == null || timer.count() == 0L) {
            return 0d;
        }

        for (ValueAtPercentile percentile : timer.takeSnapshot().percentileValues()) {
            if (percentile.percentile() >= 0.95d) {
                return percentile.value(TimeUnit.MILLISECONDS);
            }
        }

        // Fallback when no percentile snapshot is available.
        return timer.mean(TimeUnit.MILLISECONDS);
    }

    private double safeRatio(double numerator, double denominator) {
        if (denominator <= 0d) {
            return 0d;
        }
        return numerator / denominator;
    }
}
