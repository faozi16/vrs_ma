package com.af.vrs.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.catalog.cutover.gates")
public class CatalogCutoverGateProperties {
    private boolean enabled = true;
    private double minSuccessRatio = 0.95d;
    private double maxP95LatencyMs = 400d;
    private double maxFailureRatio = 0.05d;
    private long maxCircuitOpenCount = 2L;
    private long maxFallbackHitCount = 5L;
    private long minObservedCalls = 10L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getMinSuccessRatio() {
        return minSuccessRatio;
    }

    public void setMinSuccessRatio(double minSuccessRatio) {
        this.minSuccessRatio = minSuccessRatio;
    }

    public double getMaxP95LatencyMs() {
        return maxP95LatencyMs;
    }

    public void setMaxP95LatencyMs(double maxP95LatencyMs) {
        this.maxP95LatencyMs = maxP95LatencyMs;
    }

    public double getMaxFailureRatio() {
        return maxFailureRatio;
    }

    public void setMaxFailureRatio(double maxFailureRatio) {
        this.maxFailureRatio = maxFailureRatio;
    }

    public long getMaxCircuitOpenCount() {
        return maxCircuitOpenCount;
    }

    public void setMaxCircuitOpenCount(long maxCircuitOpenCount) {
        this.maxCircuitOpenCount = maxCircuitOpenCount;
    }

    public long getMaxFallbackHitCount() {
        return maxFallbackHitCount;
    }

    public void setMaxFallbackHitCount(long maxFallbackHitCount) {
        this.maxFallbackHitCount = maxFallbackHitCount;
    }

    public long getMinObservedCalls() {
        return minObservedCalls;
    }

    public void setMinObservedCalls(long minObservedCalls) {
        this.minObservedCalls = minObservedCalls;
    }
}
