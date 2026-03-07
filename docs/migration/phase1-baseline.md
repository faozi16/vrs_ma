# Phase 1 Baseline Snapshot

Date: 2026-03-07
Scope: Monolith-to-microservices migration gate (pre-refactor baseline)

## 1. Test Baseline

Command:
```bash
./gradlew test
```

Result:
- Status: BUILD SUCCESSFUL
- Notes: Baseline verification completed before structural migration changes.

## 2. Current API Contract Inventory (Controller Mappings)

This list captures endpoint paths as implemented in the monolith at baseline time.

### Home
- `GET /`

### Auth
- Base: `/api/auth`
- `POST /api/auth/login`

### Customers
- Base: `/api/customers`
- `POST /api/customers/create`
- `GET /api/customers/get`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

### Vehicles
- Base: `/api/vehicles`
- `POST /api/vehicles/create`
- `GET /api/vehicles/get`
- `GET /api/vehicles/{id}`
- `PUT /api/vehicles/{id}`
- `DELETE /api/vehicles/{id}`

### Drivers
- Base: `/api/drivers`
- `POST /api/drivers/create`
- `GET /api/drivers/get`
- `GET /api/drivers/{id}`
- `PUT /api/drivers/{id}`
- `DELETE /api/drivers/{id}`

### Reservations
- Base: `/api/reservations`
- `POST /api/reservations/create`
- `GET /api/reservations/get`
- `GET /api/reservations/{id}`
- `PUT /api/reservations/{id}`
- `DELETE /api/reservations/{id}`

### Payments
- Base: `/api/payments`
- `POST /api/payments/create`
- `GET /api/payments/get`
- `GET /api/payments/{id}`
- `PUT /api/payments/{id}`
- `DELETE /api/payments/{id}`

### Payment Methods
- Base: `/api/payment-methods`
- `POST /api/payment-methods/create`
- `GET /api/payment-methods/get`
- `GET /api/payment-methods/{id}`
- `PUT /api/payment-methods/{id}`
- `DELETE /api/payment-methods/{id}`

### Feedbacks
- Base: `/api/feedbacks`
- `POST /api/feedbacks`
- `GET /api/feedbacks/get`
- `GET /api/feedbacks/{id}`
- `PUT /api/feedbacks/{id}`
- `DELETE /api/feedbacks/{id}`

## 3. Domain Mapping Baseline (For Phase 2 Packaging)

- Customer domain: `Customer`, `PaymentMethod`, auth/security classes.
- Catalog domain: `Vehicle`, `Driver`.
- Reservation domain: `Reservation`, `Feedback`.
- Billing domain: `Payment`.

## 4. Regression Policy For Next Phases

- Preserve endpoint behavior unless explicitly approved.
- Run `./gradlew test` after each migration phase.
- Compare updated API surface against this snapshot before advancing to extraction phases.
