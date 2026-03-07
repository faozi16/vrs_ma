# Phase 5 Remote Read Hardening

Date: 2026-03-07
Scope: Reliability settings and facade parity tests for remote Catalog mode

## 1. Goal

Increase confidence for Catalog extraction by hardening remote facade calls and validating local/remote facade behavior at the contract boundary.

## 2. Resilience Improvements

Updated `RemoteCatalogReadFacadeAdapter` with configurable resilience:
- Connect timeout (`app.catalog.remote.connect-timeout-ms`)
- Read timeout (`app.catalog.remote.read-timeout-ms`)
- Bounded retries (`app.catalog.remote.max-attempts`)
- Retry backoff (`app.catalog.remote.retry-backoff-ms`)

Behavior:
- Successful response maps to `VehicleSummary`.
- Remote failures are retried up to max attempts, then return empty result.

## 3. Runtime Profile for Remote Mode

Added `src/main/resources/application-remote.properties`:

```properties
app.catalog.read.mode=remote
app.catalog.remote.base-url=http://localhost:8002
app.catalog.remote.connect-timeout-ms=2000
app.catalog.remote.read-timeout-ms=3000
app.catalog.remote.max-attempts=2
app.catalog.remote.retry-backoff-ms=100
```

This allows launching with `--spring.profiles.active=remote` to exercise remote facade mode.

## 4. Contract-Style Facade Tests

Added tests:
- `CatalogReadFacadeAdapterTest`
  - verifies local adapter mapping and availability semantics.
- `RemoteCatalogReadFacadeAdapterTest`
  - verifies remote response mapping.
  - verifies retry behavior on repeated 503 failure.

These tests validate parity at the facade boundary, independent of controller APIs.

## 5. Compatibility Statement

- Default `local` mode remains unchanged.
- Controller/API contracts are unchanged.
- Changes are additive and extraction-focused.
