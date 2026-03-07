# Architecture Enhancement Assessment & Impact Document

**Vehicle Reservation System (VRS)**  
**Date**: February 18, 2026  
**Version**: 1.0 - Initial Assessment  
**Prepared by**: Architecture Review Team

---

## Executive Summary

This document provides a comprehensive assessment of two major enhancement initiatives:

1. **Monolithic → Microservices Transformation**
2. **Multi-Vehicle Type Support Enhancement**

### Current State
- Monolithic Spring Boot 3 application
- 7 core entities serving car rental scenarios
- Single database (MySQL)
- CRUD REST API at `/api/*`

### Proposed Outcome
- Microservices-based architecture (4-5 independent services)
- Support for multiple vehicle types (motorcycle, car, bus, truck, etc.)
- Event-driven inter-service communication
- Type-specific pricing, availability, and operational rules
- Enhanced scalability and resilience

### Impact Summary
| Aspect | Impact | Risk |
|--------|--------|------|
| Code Structure | High | Medium |
| Database Design | High | Medium |
| API Contracts | High | Low |
| Data Restructuring | Moderate | Medium |
| Testing | High | Medium |
| DevOps/Deployment | Moderate | Medium |

**Risk Level**: Medium-High (distributed systems complexity, eventual consistency)

---

## Part 1: Monolithic to Microservices Transformation

### 1.1 Current Monolithic Architecture

```mermaid
graph TB
    Client["Client Layer"]
    API["API Layer<br/>7 Controllers"]
    Service["Service Layer<br/>7 Services"]
    Mapper["MapStruct Mappers"]
    Repository["Data Access Layer<br/>7 Repositories"]
    MySQL["MySQL Database<br/>Single instance"]
    
    Client -->|REST| API
    API --> Service
    Service --> Mapper
    Service --> Repository
    Repository --> MySQL
    
    style MySQL fill:#ff9999
    style API fill:#99ccff
    style Service fill:#99ff99
    style Repository fill:#ffcc99
```

#### Disadvantages of Current Monolithic Approach
1. **Scalability**: Entire app scales together; can't scale individual features
2. **Technology Lock-in**: All services bound to Spring Boot/Java/MySQL
3. **Team Silos**: Hard to have independent optimization by business function
4. **Deployment Risk**: Single failure point affects all functionality
5. **Database Coupling**: All business logic tightly coupled via shared DB
6. **Slow Development Cycles**: Requires full app rebuild/test for minor changes

---

### 1.2 Proposed Microservices Architecture

#### Recommended Service Split (4 Services)

```mermaid
graph TB
    Client["Client (Web/Mobile/API)"]
    Gateway["API Gateway<br/>Port: 8000"]
    
    CustomerService["Customer Service<br/>Port: 8001<br/>- Auth<br/>- Profiles<br/>- Payment Methods"]
    CatalogService["Catalog Service<br/>Port: 8002<br/>- Vehicle Types<br/>- Inventory<br/>- Pricing"]
    ReservationService["Reservation Service<br/>Port: 8003<br/>- Bookings<br/>- Status Tracking<br/>- Assignments"]
    BillingService["Billing Service<br/>Port: 8004<br/>- Payments<br/>- Invoices<br/>- Ledger"]
    
    CustomerDB[(Customer DB)]
    CatalogDB[(Catalog DB)]
    ReservationDB[(Reservation DB)]
    BillingDB[(Billing DB)]
    
    MessageBroker["Message Broker<br/>RabbitMQ/Kafka"]
    
    Client -->|HTTP| Gateway
    Gateway -->|REST| CustomerService
    Gateway -->|REST| CatalogService
    Gateway -->|REST| ReservationService
    Gateway -->|REST| BillingService
    
    CustomerService --> CustomerDB
    CatalogService --> CatalogDB
    ReservationService --> ReservationDB
    BillingService --> BillingDB
    
    CustomerService -.->|Events| MessageBroker
    CatalogService -.->|Events| MessageBroker
    ReservationService -.->|Events| MessageBroker
    BillingService -.->|Events| MessageBroker
    
    style Gateway fill:#ff6b6b
    style CustomerService fill:#4ecdc4
    style CatalogService fill:#45b7d1
    style ReservationService fill:#f7b731
    style BillingService fill:#5f27cd
```

#### Service Breakdown

##### **1. Customer Service** (Port 8001)
**Responsibility**: User management, authentication, profiles, payment methods

**Endpoints**:
```
POST   /api/customers/register
GET    /api/customers/{id}
PUT    /api/customers/{id}
DELETE /api/customers/{id}
POST   /api/customers/{id}/payment-methods
GET    /api/customers/{id}/payment-methods
```

**Database**: `customer_db` (PostgreSQL recommended)

**Entities**:
- Customer
- PaymentMethod
- CustomerProfile

**Dependencies**: None (independent)

---

##### **2. Catalog Service** (Port 8002)
**Responsibility**: Vehicle inventory, types, models, pricing, availability

**Endpoints**:
```
GET    /api/vehicle-types              # List all vehicle types
GET    /api/vehicles                   # List vehicles with filtering
GET    /api/vehicles/{id}
POST   /api/vehicles                   # Admin: add vehicle
PUT    /api/vehicles/{id}
GET    /api/pricing/{vehicleTypeId}    # Price by type & duration
```

**Database**: `catalog_db` (PostgreSQL)

**Entities**:
- VehicleType (NEW)
- Vehicle (enhanced)
- PricingRule (NEW)
- VehicleFeatures (NEW)
- Availability (NEW)

**Key Enhancements**:
- Vehicle discriminator by type (motorcycle, car, bus, etc.)
- Type-specific pricing rules (per hour, per day, per km)
- Type-specific availability windows
- Type-specific insurance tiers

---

##### **3. Reservation Service** (Port 8003)
**Responsibility**: Booking management, reservation lifecycle, assignments

**Endpoints**:
```
POST   /api/reservations/create
GET    /api/reservations/{id}
PUT    /api/reservations/{id}/status
DELETE /api/reservations/{id}/cancel
GET    /api/reservations/availability    # Check real-time availability
POST   /api/reservations/{id}/driver-assignment
GET    /api/feedback
POST   /api/feedback
```

**Database**: `reservation_db` (PostgreSQL)

**Entities**:
- Reservation
- ReservationStatus (NEW - enum)
- DriverAssignment (NEW)
- Feedback

**Public Events**:
```
reservation.created
reservation.confirmed
reservation.canceled
reservation.completed
driver.assigned
```

**Consumed Events**:
- `payment.completed` → confirm reservation
- `vehicle.unavailable` → adjust availability

---

##### **4. Billing Service** (Port 8004)
**Responsibility**: Payment processing, invoicing, financial ledger

**Endpoints**:
```
POST   /api/payments/process
GET    /api/payments/{id}
GET    /api/invoices/{reservationId}
GET    /api/billing-ledger
```

**Database**: `billing_db` (PostgreSQL)

**Entities**:
- Payment
- Invoice (NEW)
- BillingLedger (NEW)
- BillingAudit (NEW)

**Public Events**:
```
payment.initiated
payment.completed
payment.failed
invoice.generated
```

**Consumed Events**:
- `reservation.completed` → generate invoice

---

### 1.3 Supporting Infrastructure

#### API Gateway (Spring Cloud Gateway)
- Central entry point
- Request routing to services
- Authentication/authorization
- Rate limiting
- Request/response logging

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: customer-service
          uri: http://localhost:8001
          predicates:
            - Path=/api/customers/**
        - id: catalog-service
          uri: http://localhost:8002
          predicates:
            - Path=/api/vehicles/**,/api/vehicle-types/**
        # ... more routes
```

#### Message Broker (RabbitMQ/Apache Kafka)
- Asynchronous event publishing
- Service decoupling
- Event replay capability
- Guaranteed message delivery

**Events**:
```
reservation.*
payment.*
vehicle.*
customer.*
```

#### Service Discovery (Eureka or Consul)
- Automatic service registration
- Health monitoring
- Load balancing
- Failover handling

#### Distributed Tracing (Jaeger/Zipkin)
- Request flow tracking across services
- Performance bottleneck identification
- Debugging complex issues

#### Configuration Management (Spring Cloud Config)
- Centralized configuration
- Environment-specific properties
- Dynamic property updates

---

### 1.4 Data Restructuring Strategy (Non-Live System)

#### Stage 1: Database Separation Design

**Before**:
```
MySQL: car_rsvt
├── customer (all user data)
├── vehicle (inventory)
├── driver (staff)
├── reservation (bookings)
├── payment (transactions)
├── payment_method (payment info)
└── feedback (reviews)
```

**After**:
```
PostgreSQL: customer_db        PostgreSQL: catalog_db         PostgreSQL: reservation_db    PostgreSQL: billing_db
├── customer               ├── vehicle_type            ├── reservation          ├── payment
├── payment_method         ├── vehicle                 ├── feedback             ├── invoice
└── customer_audit         ├── pricing_rule            ├── driver_assignment    └── billing_audit
                           ├── vehicle_features        └── reservation_status
                           └── availability
```

#### Stage 2: Data Transformation and Initial Seeding

**Step 1**: Extract baseline reference data from current monolith schema
```sql
-- Customer data → customer_db
SELECT * FROM customer WHERE id > 0;

-- Vehicle data → catalog_db
-- Create default vehicle_type entries
INSERT INTO vehicle_type (name, code, capacity, hourly_rate)
VALUES ('Car', 'CAR', 5, 50.00);

-- Map existing vehicles to types
INSERT INTO vehicle_type_mapping 
SELECT DISTINCT vehicle_type FROM vehicle;
```

**Step 2**: Transform and load into service-owned databases
```
customer_db ← Monolithic DB (customer, payment_method, customer_audit)
catalog_db ← Monolithic DB (vehicle) + New data (vehicle_type, pricing_rules)
reservation_db ← Monolithic DB (reservation, feedback, driver_assignment)
billing_db ← Monolithic DB (payment) + New entities (invoice, ledger)
```

**Step 3**: Validation and reconciliation
```
Total records before = Total records after
Implement data auditing triggers
Run reconciliation jobs until all validation checks pass
```

#### Stage 3: Microservices-Only Enablement
- Provision and initialize service-owned databases
- Enable microservices as primary runtime
- Verify end-to-end workflows in integrated environment
- Decommission monolithic runtime after sign-off

---

### 1.5 Implementation Roadmap

```
Stage A: Infrastructure Setup
├── Set up API Gateway
├── Configure message broker (RabbitMQ/Kafka)
├── Set up service discovery (Eureka)
├── Deploy monitoring (Jaeger, Prometheus)
└── Create deployment pipelines

Stage B: Service Development (parallel)
├── Customer Service (standalone)
├── Catalog Service (with Vehicle Types)
├── Reservation Service (event producers)
└── Billing Service (event consumers)

Stage C: Integration & Testing
├── Event publishing/consuming
├── Integration tests across services
├── Load testing
└── Failover scenarios

Stage D: Full Deployment
├── Database initialization and validation
├── Microservices runtime activation
├── End-to-end business flow verification
└── Monitoring and fixes
```

### 1.6 Phase 1 Baseline (Executed: 2026-03-07)

This section records the approved migration gate before any structural monolith-to-microservices changes.

#### Phase 1 Objective
- Establish a reproducible baseline for behavior, test health, and API surface.
- Prevent regressions during modular-monolith and service extraction phases.

#### Completed Activities
- Verified clean git working tree before phase execution.
- Executed test baseline from repository root: `./gradlew test`.
- Captured current API endpoint contract inventory from controller mappings.
- Linked artifacts for future delta comparison.

#### Baseline Result
- Test status: `BUILD SUCCESSFUL`.
- Scope: existing monolith runtime and controller API surface.

#### Baseline Artifacts
- API and migration baseline snapshot: `docs/migration/phase1-baseline.md`.

#### Exit Criteria For Phase 1
- Green tests on baseline commit.
- Frozen endpoint inventory documented.
- Assessment document updated with migration gate evidence.

#### Entry Criteria For Phase 2
- Start modular-monolith packaging by domain boundaries.
- Keep API behavior backward-compatible with the baseline snapshot.
- Run full regression tests after each domain move.

### 1.7 Phase 2 Modular Monolith (Executed: 2026-03-07)

This section records the first structural code change while preserving monolith deployment and API behavior.

#### Phase 2 Objective
- Introduce domain application boundaries inside the monolith.
- Keep all endpoint contracts and request/response behavior backward-compatible.

#### Completed Activities
- Added domain application interfaces for target microservice boundaries:
    - `customer` (`CustomerApplicationService`, `PaymentMethodApplicationService`)
    - `catalog` (`VehicleApplicationService`, `DriverApplicationService`)
    - `reservation` (`ReservationApplicationService`, `FeedbackApplicationService`)
    - `billing` (`PaymentApplicationService`)
- Updated existing service implementations to implement those interfaces.
- Updated controllers to depend on domain interfaces instead of concrete service classes.
- Updated controller unit tests to mock domain interfaces.

#### Baseline Compatibility Check
- Endpoint paths: unchanged.
- Payload/response mapping: unchanged.
- Test status after refactor: `BUILD SUCCESSFUL` via `./gradlew test`.

#### Artifacts
- Phase 1 baseline: `docs/migration/phase1-baseline.md`
- Phase 2 implementation notes: `docs/migration/phase2-modular-monolith.md`

#### Entry Criteria For Phase 3
- Introduce explicit internal module facades for cross-domain orchestration.
- Begin service extraction preparation (starting with Catalog service) with API compatibility checks against Phase 1 baseline.

### 1.8 Phase 3 Extraction Preparation (Executed: 2026-03-07)

This section records extraction-readiness work after package normalization to `com.af.vrs`.

#### Phase 3 Objective
- Introduce explicit cross-domain read facades to reduce direct package coupling.
- Prepare Catalog-first extraction by defining stable in-process contracts that can later be remoted.

#### Completed Activities
- Added shared facade contracts:
    - `CustomerReadFacade`
    - `CatalogReadFacade`
    - `ReservationReadFacade`
    - `BillingReadFacade`
- Added facade adapters per domain using existing repositories:
    - `customer/facade/CustomerReadFacadeAdapter`
    - `catalog/facade/CatalogReadFacadeAdapter`
    - `reservation/facade/ReservationReadFacadeAdapter`
    - `billing/facade/BillingReadFacadeAdapter`
- Added shared summary models for facade payload boundaries:
    - `CustomerSummary`, `VehicleSummary`, `ReservationSummary`
- Added reservation cross-domain orchestration component:
    - `ReservationCrossDomainService`
    - `ReferenceValidationResult`
- Added billing repository contract methods for extraction-ready lookups:
    - `existsByReservationId(Long reservationId)`
    - `findByReservationId(Long reservationId)`

#### Baseline Compatibility Check
- External REST endpoints: unchanged.
- Controller request/response shape: unchanged.
- Deployment mode: still single-process monolith.

#### Artifacts
- Phase 3 implementation notes: `docs/migration/phase3-extraction-preparation.md`

#### Entry Criteria For Phase 4
- Use the shared facades as the only cross-domain access path for new logic.
- Start Catalog service extraction behind `CatalogReadFacade` boundary.
- Add compatibility adapters (local vs remote) without changing controller contracts.

---

## Part 2: Multi-Vehicle Type Support Enhancement

### 2.1 Current Vehicle Type Handling

**Current Implementation**:
```java
public class Vehicle {
    private Long vehicleId;
    private String vehicleType;      // Just a string: "car", "motorcycle", etc.
    private String licensePlate;
    private String status;
    private Driver driver;
}
```

**Issues**:
- Vehicle type is just a string (no constraints)
- No type-specific pricing
- No type-specific capacity/features
- No type-specific availability rules
- No type-specific insurance tiers
- Pricing is generic (not per-type)

---

### 2.2 Enhanced Vehicle Type Architecture

#### New Data Model

```mermaid
erDiagram
    VEHICLE_TYPE ||--o{ VEHICLE : "defines"
    VEHICLE_TYPE ||--o{ PRICING_RULE : "has"
    VEHICLE_TYPE ||--o{ VEHICLE_FEATURE : "includes"
    VEHICLE ||--o{ RESERVATION : "booked_for"
    RESERVATION ||--o{ PRICING_CALCULATION : "uses"
    VEHICLE_TYPE ||--o{ INSURANCE_TIER : "has"

    VEHICLE_TYPE {
        BIGINT type_id PK
        STRING name "motorcycle, car, bus, truck"
        STRING code "MCY, CAR, BUS, TRK"
        INT passenger_capacity
        DECIMAL base_hourly_rate
        DECIMAL base_daily_rate
        TEXT description
        STRING status "ACTIVE, INACTIVE"
        INT insurance_tier
    }

    VEHICLE {
        BIGINT vehicle_id PK
        BIGINT type_id FK "link to vehicle_type"
        STRING license_plate
        STRING model_year
        STRING color
        STRING status "AVAILABLE, RENTED, MAINTENANCE"
    }

    PRICING_RULE {
        BIGINT pricing_rule_id PK
        BIGINT type_id FK
        STRING rule_name
        DECIMAL hourly_multiplier
        DECIMAL daily_multiplier
        STRING time_period "PEAK, OFF_PEAK, WEEKEND"
        DATETIME effective_from
    }

    VEHICLE_FEATURE {
        BIGINT feature_id PK
        BIGINT type_id FK
        STRING feature_name "AC, GPS, WiFi, Camera"
        BOOLEAN included_in_base_price
        DECIMAL additional_cost
    }

    INSURANCE_TIER {
        BIGINT insurance_tier_id PK
        BIGINT type_id FK
        STRING tier_name "BASIC, STANDARD, PREMIUM"
        DECIMAL daily_cost
        DECIMAL deductible
        TEXT coverage_details
    }

    RESERVATION {
        BIGINT reservation_id PK
        BIGINT type_id FK "vehicle_type at booking time"
        BIGINT vehicle_id FK "actual vehicle assigned"
    }

    PRICING_CALCULATION {
        BIGINT calc_id PK
        BIGINT reservation_id FK
        BIGINT type_id FK
        DECIMAL base_amount
        DECIMAL surcharge_amount
        DECIMAL insurance_amount
        DECIMAL total_amount
    }
```

#### SQL Schema Changes

```sql
-- New table: vehicle_type
CREATE TABLE vehicle_type (
    type_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,      -- MCY, CAR, BUS, TRK
    passenger_capacity INT,
    cargo_capacity DECIMAL(10,2),          -- in liters
    base_hourly_rate DECIMAL(10,2),
    base_daily_rate DECIMAL(10,2),
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    fuel_type VARCHAR(20),                 -- PETROL, DIESEL, EV
    transmission VARCHAR(20),              -- MANUAL, AUTO
    created_at TIMESTAMP DEFAULT NOW()
) ENGINE=InnoDB;

-- Enhanced vehicle table
ALTER TABLE vehicle ADD COLUMN (
    type_id BIGINT NOT NULL FOREIGN KEY REFERENCES vehicle_type(type_id),
    model_year INT,
    color VARCHAR(50),
    fuel_type VARCHAR(20),
    transmission VARCHAR(20),
    mileage_km INT DEFAULT 0,
    registration_expiry DATE,
    insurance_expiry DATE,
    maintenance_due_km INT
);

-- New table: pricing_rule
CREATE TABLE pricing_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_id BIGINT NOT NULL FOREIGN KEY REFERENCES vehicle_type(type_id),
    rule_name VARCHAR(100),
    rule_type VARCHAR(20),                 -- HOURLY, DAILY, KM_BASED
    hourly_multiplier DECIMAL(5,2) DEFAULT 1.0,
    daily_multiplier DECIMAL(5,2) DEFAULT 1.0,
    km_rate DECIMAL(10,2),                 -- per km
    time_period VARCHAR(20),               -- PEAK, OFF_PEAK, WEEKEND
    effective_from DATETIME,
    effective_to DATETIME,
    priority INT
) ENGINE=InnoDB;

-- New table: vehicle_feature
CREATE TABLE vehicle_feature (
    feature_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_id BIGINT NOT NULL FOREIGN KEY REFERENCES vehicle_type(type_id),
    feature_name VARCHAR(100),
    included_in_base BOOLEAN DEFAULT FALSE,
    additional_cost DECIMAL(10,2) DEFAULT 0,
    description TEXT
) ENGINE=InnoDB;

-- New table: insurance_tier
CREATE TABLE insurance_tier (
    tier_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_id BIGINT NOT NULL FOREIGN KEY REFERENCES vehicle_type(type_id),
    tier_name VARCHAR(50),                 -- BASIC, STANDARD, PREMIUM
    daily_cost DECIMAL(10,2),
    deductible DECIMAL(10,2),
    max_coverage DECIMAL(10,2),
    coverage_details TEXT
) ENGINE=InnoDB;

-- Pricing calculation history
CREATE TABLE pricing_calculation (
    calc_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL FOREIGN KEY REFERENCES reservation(reservation_id),
    type_id BIGINT NOT NULL FOREIGN KEY REFERENCES vehicle_type(type_id),
    base_amount DECIMAL(10,2),
    hourly_rate DECIMAL(10,2),
    daily_rate DECIMAL(10,2),
    km_rate DECIMAL(10,2),
    hours_reserved INT,
    days_reserved INT,
    km_reserved INT,
    subtotal DECIMAL(10,2),
    surcharge_amount DECIMAL(10,2),
    tax_amount DECIMAL(10,2),
    insurance_tier_id BIGINT FOREIGN KEY REFERENCES insurance_tier(tier_id),
    insurance_amount DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    calculated_at TIMESTAMP DEFAULT NOW()
) ENGINE=InnoDB;
```

---

### 2.3 Vehicle Type Variations & Capabilities

#### Supported Vehicle Types

| Type | Capacity | Features | Pricing Model | Insurance | Status |
|------|----------|----------|---------------|-----------|--------|
| **Motorcycle** | 1-2 | Helmet, Navigation | Hourly-focused | Basic | ✓ Implement Phase 1 |
| **Car** | 4-5 | AC, GPS, WiFi | Hourly/Daily mix | Standard | ✓ Implement Phase 1 |
| **Sedan** | 4-5 | Luxury, All-wheel | Hourly/Daily | Premium | Phase 2 |
| **SUV** | 5-7 | Large cargo, 4WD | Daily-focused | Premium | Phase 2 |
| **Bus** | 30-50 | Air suspension, WiFi | Daily/Weekly | Heavy-duty | Phase 2 |
| **Truck** | 1-3 | Cargo box, Lift | Daily/KM-based | Heavy-duty | Phase 2 |
| **Van** | 8-12 | Removable rows, Large cargo | Daily | Standard | Phase 3 |

#### Type-Specific Pricing Example

**Motorcycle**:
- Hourly: Base rate + peak surcharge
- Insurance: Basic tier
- Features: Helmet (included), GPS (optional add-on)

**Car**:
- Hourly/Daily mixed model
- Insurance: Standard tier
- Features: AC (included), WiFi (optional add-on), Dash cam (optional add-on)

**Bus**:
- Daily/Weekly model
- Insurance: Heavy-duty tier
- Per-km surcharge after threshold distance
- Fuel surcharge: varies by diesel prices

---

### 2.4 Pricing Engine Enhancements

#### Dynamic Pricing Calculation

```java
// Pseudocode for pricing calculation
public class PricingCalculator {
    
    public PricingResult calculatePrice(ReservationRequest request) {
        // 1. Get vehicle type
        VehicleType type = getVehicleType(request.vehicleTypeId);
        
        // 2. Calculate base rental cost
        BigDecimal baseCost = calculateBaseCost(
            type,
            request.pickupTime,
            request.dropoffTime,
            request.estimatedKm
        );
        
        // 3. Apply dynamic surcharges
        BigDecimal surcharge = calculateSurcharges(
            type,
            request.pickupTime,    // Peak hours?
            request.season,        // Peak season?
            request.dayOfWeek      // Weekend?
        );
        
        // 4. Add insurance cost
        BigDecimal insuranceCost = calculateInsurance(
            type,
            request.insuranceTier,
            request.durationDays
        );
        
        // 5. Calculate savings (early booking, loyalty)
        BigDecimal discount = calculateDiscount(
            request.customerId,
            request.bookingAdvanceDays
        );
        
        // 6. Apply taxes
        BigDecimal tax = (baseCost + surcharge + insuranceCost - discount) * TAX_RATE;
        
        return PricingResult(
            baseCost,
            surcharge,
            insuranceCost,
            discount,
            tax,
            baseCost + surcharge + insuranceCost - discount + tax
        );
    }
    
    private BigDecimal calculateBaseCost(VehicleType type, ...) {
        if (rentalDays <= 1) {
            return type.getHourlyRate() * hours;
        } else if (rentalDays <= 7) {
            return type.getDailyRate() * days;
        } else {
            BigDecimal weeklyRate = type.getDailyRate() * 6;  // 6x day rate
            return weeklyRate * (days / 7) + type.getDailyRate() * (days % 7);
        }
    }
}
```

---

### 2.5 Impact on Existing Features

#### Reservation Workflow Changes

**Before**:
```
Customer picks vehicle → Reserve → Pay → Done
```

**After**:
```
Customer picks vehicle TYPE → 
  Shows available types & pricing → 
  Picks specific type (auto-recommends based on needs) →
  System assigns available vehicle of that type →
  Confirm with vehicle-specific details (license plate, color, features) →
  Pay (with vehicle-type pricing) →
  Reserve
```

#### API Changes

**New Endpoints**:
```
GET /api/vehicle-types              # List all available types
GET /api/vehicle-types/{typeId}     # Get type details + pricing
GET /api/availability/check         # Check availability by type
  ?vehicleType=CAR&pickupTime=2026-02-20&duration=2days

POST /api/reservations/estimate-cost   # Get cost estimate by type
  {
    "vehicleTypeId": 2,
    "pickupTime": "2026-02-20T10:00",
    "dropoffTime": "2026-02-22T10:00",
    "insuranceTier": "STANDARD"
  }
```

**Modified Endpoints**:
```
POST /api/reservations/create
  BEFORE: { vehicleId, customerID, pickupTime, dropoffTime }
  AFTER:  { vehicleTypeId, customerID, pickupTime, dropoffTime, insuranceTier, features[] }

GET /api/reservations/{id}
  BEFORE: Shows assigned vehicle
  AFTER:  Shows vehicle type + assigned vehicle + calculated pricing
```

---

## Part 3: Combined Impact Assessment

### 3.1 Architecture Transformation + Vehicle Type Support

#### Integration Points

```mermaid
graph LR
    A["Customer Service"] -->|Lookup customer| B["Catalog Service"]
    B -->|Vehicle Type + Pricing| C["Reservation Service"]
    C -->|Booking confirm| D["Billing Service"]
    D -->|Payment complete| C
    C -->|New reservation event| B
    B -->|Update availability event| C
    
    style B fill:#ffcccc
    style D fill:#ffffcc
```

#### Database Impact

| Current | New | Reason |
|---------|-----|--------|
| 1 MySQL instance | 4 PostgreSQL instances | Service isolation, scalability |
| 7 tables | 15-20 tables (per DB) | Vehicle type support, pricing rules |
| Monolithic schema | Polyglot persistence | Each service optimized DB |
| Shared sequences | Service-local sequences | Distributed IDs (UUID recommended) |

#### Code Structure Changes

```
BEFORE: com.af.vrs/
├── controller/
├── service/
├── entity/
├── repository/
└── dto/

AFTER: 
├── vrs-api-gateway/
│   └── src/main/java/com/af/vrs/gateway/
├── vrs-customer-service/
│   ├── src/main/java/com/af/vrs/customer/
│   ├── customer.properties
│   └── docker-compose.yml
├── vrs-catalog-service/
│   ├── src/main/java/com/af/vrs/catalog/
│   ├── entity/[VehicleType, Pricing, Features, Insurance]
│   ├── catalog.properties
│   └── docker-compose.yml
├── vrs-reservation-service/
│   ├── src/main/java/com/af/vrs/reservation/
│   ├── event/[ReservationCreated, ReservationConfirmed]
│   ├── reservation.properties
│   └── docker-compose.yml
├── vrs-billing-service/
│   ├── src/main/java/com/af/vrs/billing/
│   ├── event/[PaymentProcessed, InvoiceGenerated]
│   ├── billing.properties
│   └── docker-compose.yml
├── vrs-common/
│   ├── infrastructure/
│   ├── events/
│   └── exceptions/
└── docker-compose.yml (orchestrate all services)
```

---

### 3.2 Risk Assessment

#### High-Risk Areas

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|-----------|
| **Data Loss During Restructuring** | Critical | Medium | Triple-verify, backups, validation jobs |
| **Eventual Consistency Issues** | High | High | Event sourcing, saga pattern, reconciliation |
| **Distributed Transaction Complexity** | High | High | Message broker + retry logic, DLQ |
| **Service Interdependency Bugs** | High | Medium | Contract testing, integration tests |
| **Performance Degradation** | High | Medium | Caching (Redis), service-to-service optimization |
| **Operational Complexity** | Moderate | High | Comprehensive monitoring, runbooks |

#### Medium-Risk Areas

| Risk | Impact | Mitigation |
|------|--------|-----------|
| **Vehicle Type Data Validation** | Moderate | Strong schema validation, data contracts |
| **Pricing Calculation Bugs** | Moderate | Extensive unit + integration tests |
| **Duplicate Reservations** | Moderate | Idempotency keys, optimistic locking |
| **Service Discovery Issues** | Moderate | Health checks, circuit breakers |

---

### 3.3 Non-Functional Requirements

#### Performance Targets (Target Architecture)

| Metric | Target | Current | Improvement |
|--------|--------|---------|-------------|
| API Response Time (p99) | <200ms | ~150ms | Maintain/slight increase |
| Reservation Creation | <500ms | ~300ms | Maintain with async) |
| Availability Types Listing | <100ms | New | Fast for UI dropdowns |
| Pricing Calculation | <300ms | ~200ms | Maintain with caching |
| System Uptime | 99.5% | 99.0% | Better fault isolation |

#### Scalability

| Dimension | Current | Post-Microservices |
|-----------|---------|-------------------|
| Independent Scaling | No (scales all together) | Yes (per service) |
| Database Connections | Single pool | 4 independent pools |
| Cache Strategy | None | Redis for pricing, availability |
| Load Balancing | Single instance | Per-service load balancers |

#### Security Implications

| Aspect | Change | Action |
|--------|--------|--------|
| **Authentication** | JWT tokens between services | Implement service-to-service auth (mTLS) |
| **Authorization** | Centralized in Customer Service | Implement OAuth 2.0 / OpenID Connect |
| **Data Isolation** | Shared database | Complete isolation → new challenges |
| **Secrets Management** | application.properties | Use Spring Cloud Config + Vault |
| **API Security** | Basic validation | Add API Gateway security policies |

---

### 3.4 Testing Impact

#### Test Coverage Expansion

| Test Type | Current | New |
|-----------|---------|-----|
| Unit Tests | ~40% coverage | Target 70% |
| Integration Tests | Testcontainers 1 DB | 4 services + messaging |
| Contract Tests | None | Service contracts |
| End-to-End Tests | Single app flow | Cross-service flows |
| Chaos Engineering | None | Fault injection, circuit breakers |
| Load Testing | Basic | Per-service + full stack |

---

### 3.5 Operations & Deployment

#### New Operational Requirements

1. **Container Orchestration**
   - Kubernetes (K8s) or Docker Swarm
   - Helm charts for each service
   - Service networking policies

2. **Monitoring & Logging**
   - Centralized logging (ELK/Loki)
   - Metrics collection (Prometheus)
   - Distributed tracing (Jaeger)
   - Alert management (PagerDuty integration)

3. **CI/CD Pipeline**
   - Service-specific build pipelines
   - Blue-green deployments per service
   - Canary deployments for risky changes
   - Automated rollback capability

4. **Configuration Management**
   - Spring Cloud Config server
   - Environment-specific configs (dev, staging, prod)
   - Secrets rotation

5. **Disaster Recovery**
   - Database backup strategy per service
   - Event replay capability
   - RTO/RPO targets:
     - Catalog Service: RTO=30min, RPO=5min
     - Billing Service: RTO=5min, RPO=1min (critical)

---

## Part 4: Phased Implementation Activities

### 4.1 Phase 1: Foundation
**Goal**: Core infrastructure ready, Vehicle Types basic support

**Deliverables**:
- API Gateway operational
- Message Broker configured
- Service Discovery active
- 4 databases provisioned
- Customer Service (v1) deployed
- Catalog Service with basic vehicle types
- Monitoring stack (Prometheus, Grafana)

**Vehicle Type Features**:
- 4 basic types (Motorcycle, Car, Bus, Truck)
- Simple pricing rules (base hourly/daily)
- Basic insurance tiers

---

### 4.2 Phase 2: Core Services
**Goal**: Reservation & Billing services operational

**Deliverables**:
- Reservation Service with event publishing
- Billing Service with payment processing
- Event-driven communication tested
- Service-to-service resilience (circuit breakers, retries)
- Integration tests passing

**Vehicle Type Features**:
- Dynamic pricing (peak/off-peak)
- Vehicle feature selection (AC, WiFi, etc.)
- Insurance tier selection

---

### 4.3 Phase 3: Full Architecture Activation
**Goal**: Monolithic → Microservices full adoption

**Deliverables**:
- Data restructuring validated
- All runtime flows executed on microservices
- Monolith runtime retired
- Rollback plan tested in non-production environment

**Vehicle Type Features**:
- Advanced pricing (surcharges, discounts)
- Per-type availability rules
- Advanced insurance options

---

### 4.4 Phase 4: Optimization & Scale
**Goal**: Performance tuning, advanced features

**Deliverables**:
- Caching layer (Redis) implemented
- Service-specific optimizations
- Load testing completed
- Production support runbooks
- Team training completed

**Vehicle Type Features**:
- KM-based pricing
- Multi-day/weekly discounts
- Loyalty program integration

### 4.5 Activities Log

| Activity | Owner | Status | Notes |
|----------|-------|--------|-------|
| API Gateway setup | Architecture/Platform | Pending | |
| Message broker setup | Platform | Pending | |
| Service discovery setup | Platform | Pending | |
| Customer Service extraction | Backend | Pending | |
| Catalog Service extraction + vehicle types | Backend | Pending | |
| Reservation Service extraction | Backend | Pending | |
| Billing Service extraction | Backend | Pending | |
| Service-owned database schema creation | Data/Backend | Pending | |
| Event contracts definition | Architecture/Backend | Pending | |
| Integration and contract testing | QA/Backend | Pending | |
| End-to-end scenario validation | QA/Product | Pending | |
| Monolith runtime retirement | Platform/Architecture | Pending | |

---

## Part 5: Decision Framework

### 5.1 Go/No-Go Decision Criteria

#### Before Starting, Validate:

- [ ] **Business Alignment**: Executive sponsorship confirmed
- [ ] **Team Readiness**: Microservices expertise available (or training planned)
- [ ] **Infrastructure**: Cloud/on-prem resources prepared
- [ ] **Customer Base**: Usage patterns support this complexity
- [ ] **Technical Debt**: Monolith is sufficiently problematic
- [ ] **Execution Capacity**: Team capacity available across architecture, backend, QA, and platform

#### Red Flags (Recommend Delaying):

- ⚠️ Team inexperienced with distributed systems
- ⚠️ Only 1-2 developers available
- ⚠️ Data restructuring validation not yet ready
- ⚠️ Coordination constraints across teams not yet settled
- ⚠️ No dedicated DevOps/SRE support

---

### 5.2 Full-Transformation Readiness Principles

The target direction is full transformation to microservices with vehicle type support enabled across all services. Readiness depends on:

- Clear bounded contexts and ownership per service
- Contract-first API and event design
- Service-owned data with validation gates
- Integrated observability and rollback strategy
- Cross-team execution discipline

---

## Part 6: Recommendations

### 6.1 Recommended Path Forward

**Single Recommended Path: Full Microservices + Vehicle Types Implementation**
- Execute the full transformation plan across Customer, Catalog, Reservation, and Billing services
- Apply vehicle-type enhancements as first-class capabilities in Catalog, Reservation, and Billing
- Use contract-first integration and event-driven workflows from day one
- Enforce service-owned data boundaries and operational observability before go-live

---

### 6.2 Recommended Next Steps

1. **Immediate**
   - [ ] Present recommendations to stakeholders
   - [ ] Gather feedback on phasing preference
  - [ ] Confirm full-transformation commitment and ownership
   - [ ] Form architecture review board

2. **Planning**
   - [ ] Detailed design document per service
   - [ ] Database schema for each service
   - [ ] Event schema and versioning strategy
   - [ ] API contract definitions (OpenAPI specs)
    - [ ] Data restructuring and integration test plan
   - [ ] Runbooks for failures

3. **Tooling & Infrastructure**
   - [ ] Provision development environment
   - [ ] Set up CI/CD pipelines
   - [ ] Configure monitoring/logging
   - [ ] Team training on microservices patterns

4. **Implementation**
   - [ ] Execute phased rollout per plan
   - [ ] Weekly architecture reviews
   - [ ] Continuous stakeholder updates

---

## Part 7: Appendix

### 7.1 Service Communication Patterns

#### Synchronous (REST/gRPC)
**Use When**: Need immediate response, strong consistency required

```
Customer calls Payment Service
  ↓
Payment Service checks with Billing Service for amount
  ↓
Billing Service returns total
  ↓
Payment Service confirms
```

**Pros**: Simple, consistent
**Cons**: Tight coupling, cascading failures

---

#### Asynchronous (Event-Driven)
**Use When**: Can tolerate eventual consistency, decoupled systems needed

```
Reservation Service publishes: "ReservationCreated"
  ↓ (Event Queue)
  ├→ Billing Service listens: Creates invoice
  ├→ Catalog Service listens: Updates availability
  └→ Notification Service listens: Sends confirmation email
```

**Pros**: Decoupled, scalable, resilient
**Cons**: Eventually consistent (harder to debug)

---

### 7.2 Recommended Tech Stack for Microservices

| Component | Options | Recommended |
|-----------|---------|-------------|
| Service Framework | Spring Boot, Quarkus, Micronaut | Spring Boot (familiar to team) |
| API Gateway | Spring Cloud Gateway, Kong, nginx | Spring Cloud Gateway |
| Message Broker | RabbitMQ, Kafka, AWS SNS/SQS | RabbitMQ (simpler setup) |
| Service Discovery | Eureka, Consul, Spring Cloud | Eureka (Spring ecosystem) |
| Config Server | Spring Cloud Config, Consul | Spring Cloud Config |
| Database | PostgreSQL (per service) | PostgreSQL (ACID, JSON support) |
| Caching | Redis | Redis (cache + sessions) |
| Monitoring | Prometheus + Grafana | Prometheus + Grafana |
| Tracing | Jaeger, Zipkin | Jaeger (CNCF project) |
| Container | Docker | Docker |
| Orchestration | Kubernetes, Docker Compose | Docker Compose (dev), Kubernetes (prod) |

---

### 7.3 Comparison Matrix

| Aspect | Monolithic | Microservices (Target) |
|--------|------------|------------------------|
| **Risk** | Low | Medium-High |
| **Scalability** | Limited | Excellent |
| **Ops Complexity** | Simple | Complex |
| **Vehicle Types Support** | Basic/limited | Full domain-driven support |
| **Future Scalability** | Limited | Excellent |

---

## Conclusion

### Summary

1. **Microservices transformation** is feasible and aligns with long-term scale and ownership goals.
2. **Vehicle type support** should be implemented natively in the microservices design, not as a temporary monolith extension.
3. **Recommended approach**: Execute full transformation with contract-first service boundaries and event-driven integration.
4. **Execution control** should rely on readiness gates, testing depth, and the activities log.

### Key Recommendations

✅ **DO Implement Vehicle Types** as part of the microservices target model

✅ **DO Execute Full Microservices Plan** with service-by-service readiness gates

⚠️ **CONSIDER Execution Discipline** - prioritize contract validation and integration reliability

✅ **DO Invest in Team Training** - Microservices requires different mindset

---

**Document Status**: Ready for Review
**Next Action**: Present to stakeholders for approval

---

**Prepared by**: Architecture Review Team  
**Date**: February 18, 2026  
**Version**: 1.0-Final

