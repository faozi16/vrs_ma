# Phase 7 Remote Integration and Operability Validation

Date: 2026-03-08
Scope: Integrated remote-mode runtime validation with baseline dashboards and alerts

## 1. Goal

Validate remote catalog mode using independently running service processes and establish an initial observability control plane for extraction cutover decisions.

## 2. Integrated Runtime Topology

Added `compose.phase7-remote.yaml` with dedicated extraction-validation services:
- `catalog-service` (port `8002`) running in `local` catalog mode.
- `reservation-service` (port `8080`) running with `SPRING_PROFILES_ACTIVE=remote`.
- Shared `mysql` backing store for transition-phase compatibility.
- `prometheus` scraping `/actuator/prometheus` on both app processes.
- `grafana` with pre-provisioned datasource and dashboard.

This isolates producer/consumer process boundaries while preserving current monolith data model assumptions.

## 3. Observability Additions

### Runtime and Metrics Export

Added Spring Boot Actuator + Prometheus registry dependencies and endpoint exposure:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

Enabled latency histogram support for `catalog.facade.remote.latency` to support quantile-based SLO tracking.

### Security Policy

Explicitly allowed unauthenticated read access for:
- `/actuator/health`
- `/actuator/prometheus`

This supports container health checks and pull-based Prometheus scraping without JWT handling in the monitoring plane.

## 4. Dashboards and Alerts

Added baseline observability artifacts under `ops/observability/`:
- Prometheus scrape config: `ops/observability/prometheus/prometheus.yml`
- Alert rules: `ops/observability/prometheus/alerts/catalog-remote-alerts.yml`
- Grafana provisioning and dashboard:
  - `ops/observability/grafana/provisioning/datasources/prometheus.yml`
  - `ops/observability/grafana/provisioning/dashboards/dashboards.yml`
  - `ops/observability/grafana/dashboards/catalog-remote-observability.json`

Alert coverage includes:
- high remote call failure ratio
- frequent circuit-open events
- fallback-hit spikes

## 5. Fallback Policy Decision (Phase 7)

For extraction validation runtime, fallback policy is set to fail-fast:

```text
APP_CATALOG_REMOTE_FALLBACK_TO_LOCAL=false
```

Rationale:
- surfaces real dependency failures during integration validation
- avoids hidden degradation from local read masking
- produces cleaner signal for cutover readiness

## 6. How To Run Validation

```bash
docker compose -f compose.phase7-remote.yaml up --build -d
```

Verify health and metrics:

```bash
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/actuator/prometheus | grep catalog_facade_remote
curl -fsS http://localhost:9090/-/ready
```

Open dashboards:
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin)

Shutdown:

```bash
docker compose -f compose.phase7-remote.yaml down
```

## 7. Compatibility Statement

- Default local runtime path remains unchanged (`app.catalog.read.mode=local`).
- API/controller contracts remain unchanged.
- Phase 7 changes are additive and focused on integration operability.
