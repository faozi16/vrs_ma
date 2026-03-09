# Phase 8 Cutover Readiness Gates

Date: 2026-03-09
Scope: Implement SLO-driven cutover gates and rollback signals for Catalog extraction

## 1. Goal

Convert Phase 7 observability into explicit operational go/no-go controls before full extraction cutover.

## 2. What Was Implemented

Added remote-mode health component:
- `CatalogCutoverReadinessHealthIndicator` (`catalogCutoverReadiness`)
- `CatalogCutoverGateProperties` with configurable thresholds

Health endpoint:
- `GET /actuator/health/catalogCutoverReadiness`

Indicator behavior:
- `UNKNOWN` when traffic volume is below minimum sample threshold.
- `UP` when all cutover gates pass.
- `DOWN` when any gate is breached, with `breaches` + `rollbackSuggested=true`.
- `short_circuit` remote outcomes are counted as degraded traffic in gate ratios.

## 3. Gate Definitions

Configured via `app.catalog.cutover.gates.*`:
- `min-success-ratio`
- `max-failure-ratio`
- `max-p95-latency-ms`
- `max-circuit-open-count`
- `max-fallback-hit-count`
- `min-observed-calls`

Default values are added in both:
- `application.properties`
- `application-remote.properties`

## 4. Rollback Signal

The health response includes:
- `rollbackSuggested` (boolean)
- `breaches` (list of tripped gates)

This creates a machine-readable rollback trigger for release orchestration.

## 5. Verification

Local verification command:

```bash
curl -fsS http://localhost:8080/actuator/health/catalogCutoverReadiness
```

Expected states:
- warmup period: `UNKNOWN` (insufficient data)
- healthy remote path: `UP`
- degraded path: `DOWN` with breach details

Observed in current extraction validation:
- Gate status evaluates to `DOWN` with `successRatio=0.0` and `failureRatio=1.0` under remote call degradation, which correctly blocks promotion.

## 6. Deferred For Next Iteration

- Write-path decomposition for Catalog ownership boundaries.
- Contract-test pipeline against extracted Catalog deployment.
