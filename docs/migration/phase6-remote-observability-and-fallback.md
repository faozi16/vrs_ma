# Phase 6 Remote Observability and Fallback

Date: 2026-03-07
Scope: Operational hardening for remote `CatalogReadFacade` mode

## 1. Goal

Improve runtime safety and visibility as the Catalog boundary transitions from in-process reads to remote service calls.

## 2. Observability Added

`RemoteCatalogReadFacadeAdapter` now emits Micrometer metrics:
- `catalog.facade.remote.latency` (timer)
- `catalog.facade.remote.calls` with result tags (`success`, `failure`, `empty`, `short_circuit`)
- `catalog.facade.remote.circuit` with state tags (`opened`, `closed`)
- `catalog.facade.remote.fallback` with result/reason tags

These provide operational insight for SRE dashboards and alerting.

## 3. Circuit and Fallback Controls

Added in-process circuit logic:
- Opens after configurable consecutive failures.
- Short-circuits remote calls while open.
- Auto-closes after configurable open window and resets failure counter.

Added optional local fallback path:
- `LocalCatalogFallbackReadService` reads from local repository.
- Used only when `app.catalog.remote.fallback-to-local=true`.

## 4. New Properties

```properties
app.catalog.remote.circuit.failure-threshold=3
app.catalog.remote.circuit.open-ms=5000
app.catalog.remote.fallback-to-local=true
```

Included in both `application.properties` and `application-remote.properties`.

## 5. Test Coverage

`RemoteCatalogReadFacadeAdapterTest` now validates:
- remote response mapping
- retry behavior on failure
- circuit-open short-circuit behavior with local fallback

## 6. Compatibility Statement

- No API/controller contract changes.
- Default local mode unchanged.
- Remote mode behavior is now safer and more observable.

## 7. Handoff To Phase 7

Phase 6 entry criteria have been executed in Phase 7 with:
- process-separated Catalog/Reservation runtime validation
- dashboard + alert baseline for remote metrics
- explicit fail-fast fallback policy in extraction-validation environment
