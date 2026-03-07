# Phase 3 Extraction Preparation Notes

Date: 2026-03-07
Scope: Cross-domain facade contracts and adapters for service extraction readiness

## 1. Goal

Prepare the modular monolith for microservice extraction by introducing explicit domain-to-domain contracts that can later be switched from in-process adapters to remote clients.

## 2. Added Shared Facade Contracts

- `com.af.vrs.shared.facade.CustomerReadFacade`
- `com.af.vrs.shared.facade.CatalogReadFacade`
- `com.af.vrs.shared.facade.ReservationReadFacade`
- `com.af.vrs.shared.facade.BillingReadFacade`

## 3. Added Boundary Models

- `com.af.vrs.shared.facade.model.CustomerSummary`
- `com.af.vrs.shared.facade.model.VehicleSummary`
- `com.af.vrs.shared.facade.model.ReservationSummary`

These are intentionally minimal summaries to avoid leaking full entity graphs across module boundaries.

## 4. Added In-Monolith Adapters

- `com.af.vrs.customer.facade.CustomerReadFacadeAdapter`
- `com.af.vrs.catalog.facade.CatalogReadFacadeAdapter`
- `com.af.vrs.reservation.facade.ReservationReadFacadeAdapter`
- `com.af.vrs.billing.facade.BillingReadFacadeAdapter`

Adapters currently use Spring Data repositories and preserve the same data source/runtime behavior.

## 5. Reservation Cross-Domain Orchestration Prep

- `com.af.vrs.reservation.application.ReservationCrossDomainService`
- `com.af.vrs.reservation.application.ReferenceValidationResult`

This provides a dedicated orchestration seam for future reservation checks against customer/catalog data without hard-coding repository dependencies across domains.

## 6. Billing Query Preparation

`PaymentRepository` now includes extraction-ready lookup methods:
- `boolean existsByReservationId(Long reservationId)`
- `Optional<Payment> findByReservationId(Long reservationId)`

## 7. Compatibility Statement

- API endpoints and payload shapes remain unchanged.
- The application remains a single deployable monolith.
- These changes are structural seams for upcoming service extraction.
