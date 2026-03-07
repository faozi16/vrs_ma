# Phase 2 Modular Monolith Notes

Date: 2026-03-07
Scope: Domain boundary introduction without runtime decomposition

## 1. What Changed

The monolith now has explicit domain-level application interfaces that align with the target microservice split.

### Added domain application interfaces
- `com.af.vrs.customer.application.CustomerApplicationService`
- `com.af.vrs.customer.application.PaymentMethodApplicationService`
- `com.af.vrs.catalog.application.VehicleApplicationService`
- `com.af.vrs.catalog.application.DriverApplicationService`
- `com.af.vrs.reservation.application.ReservationApplicationService`
- `com.af.vrs.reservation.application.FeedbackApplicationService`
- `com.af.vrs.billing.application.PaymentApplicationService`

### Service wiring change
- Existing `com.af.vrs.service.*` classes now implement the new domain interfaces.
- Controllers now depend on domain interfaces rather than concrete service classes.

### Test updates
- Controller unit tests now mock interface dependencies matching controller wiring.

## 2. Compatibility Statement

- External endpoint paths are unchanged.
- DTO mapping behavior is unchanged.
- Persistence model remains unchanged (single database).

## 3. Validation

Command:
```bash
./gradlew test
```

Result:
- Status: BUILD SUCCESSFUL

## 4. Why This Matters For Migration

- Establishes clear in-process seams for future service extraction.
- Reduces direct coupling between delivery layer (controllers) and service implementations.
- Allows extraction to proceed behind stable interface contracts.
