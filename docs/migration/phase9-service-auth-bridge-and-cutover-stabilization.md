# Phase 9 Service Auth Bridge and Cutover Stabilization

Date: 2026-03-09
Scope: Restore successful remote Catalog reads by adding an internal service route for extraction runtime

## 1. Goal

Resolve remote read failures caused by endpoint authorization mismatch between Reservation service calls and Catalog service JWT policy.

## 2. What Changed

### Internal Catalog Read Route

Added an internal endpoint in `VehicleController`:
- `GET /api/vehicles/internal/catalog/{id}`

This route returns the same `VehicleDto` payload as public read endpoint and is intended for in-network service-to-service calls.

### Security Allowlist

Updated `SecurityConfig` to permit the internal route without end-user JWT:
- `/api/vehicles/internal/catalog/**`

### Remote Adapter Path Configuration

`RemoteCatalogReadFacadeAdapter` now supports configurable path:
- `app.catalog.remote.vehicle-by-id-path`

Defaults:
- base profile: `/api/vehicles/{id}` (backward-compatible)
- remote profile: `/api/vehicles/internal/catalog/{id}`

Compose extraction runtime sets:
- `APP_CATALOG_REMOTE_VEHICLE_BY_ID_PATH=/api/vehicles/internal/catalog/{id}`

## 3. Validation Outcome

After applying Phase 9 changes in `compose.phase7-remote.yaml` runtime:
- reservation create calls returned HTTP `200`
- remote metrics shifted to success series:
  - `catalog_facade_remote_calls_total{result="success"}`
- cutover gate evaluated `UP`:
  - `successRatio=1.0`
  - `failureRatio=0.0`
  - `rollbackSuggested=false`

## 4. Compatibility Statement

- Public endpoint behavior remains unchanged.
- Local/default runtime path remains unchanged.
- Internal path usage is explicit and only enabled in remote extraction profile.

## 5. Next Focus

- Replace open internal allowlist with stronger service identity/authn control (mTLS or signed service token).
- Add CI contract tests for both public and internal Catalog read contracts.
