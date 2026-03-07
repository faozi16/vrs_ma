# Phase 4 Catalog Boundary Switch

Date: 2026-03-07
Scope: Configurable local/remote adapter for `CatalogReadFacade`

## 1. Goal

Prepare for Catalog service extraction by switching from direct local reads to a configuration-selectable boundary adapter, without changing consumers such as reservation orchestration.

## 2. What Was Added

### Remote response contract
- `com.af.vrs.catalog.facade.remote.CatalogVehicleResponse`

### Remote adapter
- `com.af.vrs.catalog.facade.RemoteCatalogReadFacadeAdapter`
- Enabled when: `app.catalog.read.mode=remote`
- Calls: `GET /api/vehicles/{id}` on `app.catalog.remote.base-url`
- Maps response to: `VehicleSummary`

### Local adapter mode guard
- `com.af.vrs.catalog.facade.CatalogReadFacadeAdapter`
- Enabled when: `app.catalog.read.mode=local` (default, `matchIfMissing=true`)

## 3. New Configuration

In `application.properties`:

```properties
app.catalog.read.mode=local
app.catalog.remote.base-url=http://localhost:8002
```

Mode values:
- `local`: use in-monolith repository-backed adapter.
- `remote`: use HTTP client adapter against extracted Catalog service.

## 4. Compatibility Statement

- Existing controllers and endpoint contracts are unchanged.
- Default runtime remains local monolith behavior.
- Only adapter activation changes based on configuration.

## 5. Next Step

When Catalog service is running independently, set:

```properties
app.catalog.read.mode=remote
app.catalog.remote.base-url=http://<catalog-host>:<port>
```

Then validate parity against the Phase 1 baseline endpoint behavior and reservation flow integration.
