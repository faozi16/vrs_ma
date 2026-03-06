# Vehicle Reservation System (VRS)

A Spring Boot 3 REST API for managing vehicle reservations, customers, drivers, payments, and feedback. This is a hobby project focused on continuous enhancement toward robustness and scalability.

**Status**: `0.0.1-SNAPSHOT` | **Last Updated**: February 18, 2026  
**Java**: 21 | **Framework**: Spring Boot 3.4.0 | **Database**: MySQL 5.7+

---

## 🎯 Quick Start

### Option 1: Docker (Recommended) 🐳

```bash
docker compose up -d
```

**App runs at**: http://localhost:8080

Stop with:
```bash
docker compose down
```

### Option 2: Local Development

```bash
# Setup database (MySQL)
mysql -u root -p < init.sql

# Build & run
./gradlew clean build
./gradlew bootRun
```

---

## 📋 What's Inside

**7 Core Entities**:
- Customer (users)
- Vehicle (inventory)
- Driver (staff)
- Reservation (bookings)
- Payment (transactions)
- PaymentMethod (stored payment info)
- Feedback (ratings & reviews)

**REST API**: Full CRUD endpoints at `/api/*`

**Architecture**: Controllers → Services → Repositories → MySQL

---

## 📚 Documentation

- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Complete technical guide including:
  - Architecture & design diagrams
  - Database schema (ER diagram)
  - All 7 entities and their relationships
  - Complete API endpoint reference
  - Build and deployment instructions
  - Testing strategy
  - Enhancement roadmap

---

## 🧪 Testing

```bash
./gradlew test                      # Run all tests locally
docker compose run --rm app ./gradlew test  # Run in container
```

---

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.0
- **Database**: MySQL 5.7+
- **Build**: Gradle
- **Testing**: JUnit 5 + Testcontainers
- **Mapping**: MapStruct
- **Container**: Docker & Docker Compose

---

## 🚀 Key Features

- ✅ REST API with CRUD operations for 7 entities
- ✅ JWT-based authentication (`/api/auth/login`)
- ✅ RBAC authorization (`ADMIN`, `CUSTOMER`, `DRIVER`)
- ✅ Spring Data JPA with MapStruct DTO mapping
- ✅ Docker containerization with automatic schema init
- ✅ Integration tests with Testcontainers
- ✅ Layered architecture (Controller → Service → Repository)

---

## 🔐 Authentication & Authorization

1. Get token via login endpoint:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"<username>","password":"<password>"}'
```

2. Call protected APIs with bearer token:

```bash
curl http://localhost:8080/api/vehicles/get \
  -H "Authorization: Bearer <accessToken>"
```

Notes:
- New customer registration uses `/api/customers/create` and defaults role to `CUSTOMER`.
- Role policies are enforced in `SecurityConfig` by endpoint and HTTP method.

---

## 📞 Need More Details?

See [DEVELOPMENT.md](DEVELOPMENT.md) for:
- Detailed architecture diagrams
- Complete database schema
- All API endpoints
- Build & run instructions
- Enhancement roadmap

---

**Version**: 0.0.1-SNAPSHOT  
**Repository**: /home/theuser/workspace/car_reservation_system
