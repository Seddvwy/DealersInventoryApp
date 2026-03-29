# Dealer & Vehicle Inventory Module

A production-ready multi-tenant Inventory module built as part of a **Modular Monolith** using
Spring Boot 3, JPA, and Spring Security. It manages dealers and their vehicles with clean-architecture
layering, full tenant isolation, and role-based access control.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Module Structure](#module-structure)
3. [Data Model](#data-model)
4. [Security & Tenant Enforcement](#security--tenant-enforcement)
5. [API Reference](#api-reference)
6. [Admin Endpoint & Scope Note](#admin-endpoint--scope-note)
7. [Running the Application](#running-the-application)
8. [Acceptance Checks](#acceptance-checks)
9. [Design Decisions](#design-decisions)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      HTTP Layer                                  │
│   DealerController   VehicleController   AdminController        │
└────────────┬──────────────────┬───────────────────┬────────────┘
             │                  │                   │
┌────────────▼──────────────────▼───────────────────▼────────────┐
│                    Application / Service Layer                   │
│      DealerService          VehicleService        AdminService  │
│   (tenant enforcement)   (tenant + FK checks)  (cross-tenant)  │
└────────────┬──────────────────┬───────────────────┬────────────┘
             │                  │                   │
┌────────────▼──────────────────▼───────────────────▼────────────┐
│                      Repository Layer                            │
│   DealerRepository      VehicleRepository                       │
│   (Spring Data JPA)     (Spring Data JPA + JPQL)               │
└────────────┬──────────────────┬────────────────────────────────┘
             │                  │
┌────────────▼──────────────────▼────────────────────────────────┐
│                      Domain Layer                                │
│   Dealer (entity)       Vehicle (entity)                        │
│   SubscriptionType      VehicleStatus                           │
└─────────────────────────────────────────────────────────────────┘

Cross-cutting (shared module):
  TenantContext  → thread-local UUID holder
  TenantFilter   → servlet filter: validates X-Tenant-Id header
  SecurityConfig → Spring Security: role-based URL restrictions
  GlobalExceptionHandler → RFC-7807 error responses
  PageResponse   → generic pagination envelope
```

### Modular boundaries

| Module    | Package                             | Responsibility                                 |
|-----------|-------------------------------------|------------------------------------------------|
| `dealer`  | `com.dealership.inventory.dealer`   | Dealer CRUD, subscription type                 |
| `vehicle` | `com.dealership.inventory.vehicle`  | Vehicle CRUD, filters, subscription query      |
| `admin`   | `com.dealership.inventory.admin`    | GLOBAL_ADMIN-only cross-tenant analytics       |
| `shared`  | `com.dealership.inventory.shared`   | Security, tenant context, exceptions, web utils|

---

## Module Structure

```
src/main/java/com/dealership/inventory/
├── InventoryApplication.java
├── dealer/
│   ├── controller/    DealerController.java
│   ├── domain/        Dealer.java, SubscriptionType.java
│   ├── dto/           DealerDtos.java  (Create/Update/Response records)
│   ├── mapper/        DealerMapper.java  (MapStruct)
│   ├── repository/    DealerRepository.java
│   └── service/       DealerService.java
├── vehicle/
│   ├── controller/    VehicleController.java
│   ├── domain/        Vehicle.java, VehicleStatus.java
│   ├── dto/           VehicleDtos.java  (Create/Update/Response/DealerSummary)
│   ├── mapper/        VehicleMapper.java  (MapStruct)
│   ├── repository/    VehicleRepository.java
│   └── service/       VehicleService.java
├── admin/
│   ├── controller/    AdminController.java
│   └── service/       AdminService.java
└── shared/
    ├── config/        SecurityConfig.java
    ├── domain/        (reserved for future shared value objects)
    ├── exception/     ResourceNotFoundException, TenantAccessException,
    │                  GlobalExceptionHandler
    ├── security/      TenantContext, TenantFilter, UserRole
    └── web/           PageResponse

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__create_dealers.sql
    └── V2__create_vehicles.sql
```

---

## Data Model

### Dealer

| Column            | Type         | Notes                              |
|-------------------|--------------|------------------------------------|
| `id`              | UUID         | PK, auto-generated                 |
| `tenant_id`       | UUID         | Immutable; set from `X-Tenant-Id`  |
| `name`            | VARCHAR(255) | Required                           |
| `email`           | VARCHAR(255) | Unique across the table            |
| `subscription_type` | ENUM       | `BASIC` or `PREMIUM`               |
| `created_at`      | TIMESTAMPTZ  | Auto-set on insert                 |
| `updated_at`      | TIMESTAMPTZ  | Auto-updated on change             |

### Vehicle

| Column      | Type           | Notes                                          |
|-------------|----------------|------------------------------------------------|
| `id`        | UUID           | PK, auto-generated                             |
| `tenant_id` | UUID           | Denormalized from dealer; immutable            |
| `dealer_id` | UUID           | FK → dealers.id (CASCADE DELETE)               |
| `model`     | VARCHAR(255)   | Required                                       |
| `price`     | NUMERIC(12,2)  | Must be > 0                                    |
| `status`    | ENUM           | `AVAILABLE` or `SOLD`                          |
| `created_at`| TIMESTAMPTZ    | Auto-set on insert                             |
| `updated_at`| TIMESTAMPTZ    | Auto-updated on change                         |

> **Why denormalize `tenant_id` onto `vehicles`?**
> It allows the most common queries (list vehicles for a tenant) to avoid joining to `dealers`,
> keeping them a single-table scan on an indexed column. The FK to `dealers` still guarantees
> referential integrity.

---

## Security & Tenant Enforcement

### X-Tenant-Id Header (TenantFilter)

Every request **must** include the `X-Tenant-Id` header with a valid UUID.

```
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

| Condition               | HTTP Status | Response                                |
|-------------------------|-------------|-----------------------------------------|
| Header absent           | `400`       | `{"error": "Missing required header: X-Tenant-Id"}` |
| Header is not a UUID    | `400`       | `{"error": "X-Tenant-Id must be a valid UUID"}`      |
| Valid UUID              | –           | Stored in `TenantContext`, request proceeds          |

### Authentication

HTTP Basic authentication (demo). Replace with JWT/OAuth2 in production.

| Username       | Password | Role           |
|----------------|----------|----------------|
| `tenant-user`  | `secret` | `TENANT_USER`  |
| `global-admin` | `secret` | `GLOBAL_ADMIN` |

### Cross-Tenant Access Prevention (403)

The service layer distinguishes two failure modes on single-resource lookups:

```
findByIdAndTenantId(id, currentTenantId) → Optional.empty()
  └─ existsById(id) == true  → TenantAccessException (403)
  └─ existsById(id) == false → ResourceNotFoundException (404)
```

This approach avoids information leakage (a tenant cannot determine whether
a resource exists in another tenant purely from the error code).

### Role Authorization

| Endpoint pattern | Required role  |
|------------------|----------------|
| `/admin/**`      | `GLOBAL_ADMIN` |
| All others       | `TENANT_USER` or `GLOBAL_ADMIN` |

The admin endpoint is doubly guarded: URL-level (SecurityConfig) **and**
method-level (`@PreAuthorize("hasRole('GLOBAL_ADMIN')")`).

---

## API Reference

All endpoints (except admin) require:
- `Authorization: Basic <base64>` header
- `X-Tenant-Id: <uuid>` header

### Dealers

#### `POST /dealers`
Create a dealer for the current tenant.

**Request body:**
```json
{
  "name": "Acme Motors",
  "email": "acme@example.com",
  "subscriptionType": "PREMIUM"
}
```

**Response `201 Created`:**
```json
{
  "id": "...",
  "tenantId": "...",
  "name": "Acme Motors",
  "email": "acme@example.com",
  "subscriptionType": "PREMIUM",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

---

#### `GET /dealers/{id}`
Fetch a single dealer. Returns `404` if not found, `403` if cross-tenant.

---

#### `GET /dealers?page=0&size=20&sortBy=name&sortDir=asc`
Paginated list of dealers for the current tenant.

**Response `200 OK`:**
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

---

#### `PATCH /dealers/{id}`
Partial update. Only non-null fields are applied.

```json
{ "subscriptionType": "BASIC" }
```

---

#### `DELETE /dealers/{id}`
Returns `204 No Content`. Cascades to delete associated vehicles.

---

### Vehicles

#### `POST /vehicles`
```json
{
  "dealerId": "...",
  "model": "Tesla Model 3",
  "price": 45000.00,
  "status": "AVAILABLE"
}
```

The `dealerId` must exist within the current tenant's scope.

---

#### `GET /vehicles/{id}`

---

#### `GET /vehicles` – with filters

| Query param    | Type            | Description                                       |
|----------------|-----------------|---------------------------------------------------|
| `model`        | string          | Case-insensitive partial match                    |
| `status`       | `AVAILABLE/SOLD`| Exact match                                       |
| `priceMin`     | decimal         | Inclusive lower bound                             |
| `priceMax`     | decimal         | Inclusive upper bound                             |
| `subscription` | `BASIC/PREMIUM` | Filter by dealer's subscription type (see below)  |
| `page`         | int (default 0) | Zero-based page index                             |
| `size`         | int (default 20)| Page size                                         |
| `sortBy`       | string          | Field name (default `model`)                      |
| `sortDir`      | `asc/desc`      | Sort direction (default `asc`)                    |

**Subscription filter example:**
```
GET /vehicles?subscription=PREMIUM&page=0&size=10
```
Returns vehicles whose dealer has `subscriptionType=PREMIUM`, **scoped to the caller's tenant**.

> Note: When `subscription` is provided, vehicle-level filters (`model`, `status`, `priceMin`,
> `priceMax`) are not applied in the same query. This is a deliberate design choice keeping
> queries focused. Extend `findByDealerSubscription` to accept additional parameters if needed.

---

#### `PATCH /vehicles/{id}`
```json
{ "status": "SOLD" }
```

---

#### `DELETE /vehicles/{id}`
Returns `204 No Content`.

---

### Admin (GLOBAL_ADMIN only)

#### `GET /admin/dealers/countBySubscription`

Returns the dealer count grouped by subscription type.

**Response `200 OK`:**
```json
{
  "BASIC": 12,
  "PREMIUM": 5
}
```

> ⚠️ **Scope: GLOBAL (cross-tenant).** This count includes dealers from **all tenants**,
> not just the tenant in `X-Tenant-Id`. The header is still required (by `TenantFilter`)
> but the admin service ignores it — it queries the full table.
>
> **Rationale:** The purpose of this endpoint is platform-wide subscription analytics
> for the operator. If per-tenant counts were needed, a different endpoint with
> tenant-scoping would be appropriate.

---

## Running the Application

### Prerequisites
- Java 17
- PostgreSQL 15+ (or use Docker)
- Maven 3.9+

### Start PostgreSQL via Docker
```bash
docker run -d \
  --name inventory-db \
  -e POSTGRES_DB=inventory_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### Run the application
```bash
cd inventory-module
mvn spring-boot:run
```

Flyway will automatically run the migrations on startup.

### Example cURL calls

```bash
TENANT="550e8400-e29b-41d4-a716-446655440000"
AUTH="tenant-user:secret"

# Create a dealer
curl -s -u "$AUTH" -X POST http://localhost:8080/dealers \
  -H "X-Tenant-Id: $TENANT" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Motors","email":"acme@example.com","subscriptionType":"PREMIUM"}'

# List dealers (paginated)
curl -s -u "$AUTH" "http://localhost:8080/dealers?page=0&size=5" \
  -H "X-Tenant-Id: $TENANT"

# Get PREMIUM vehicles
curl -s -u "$AUTH" "http://localhost:8080/vehicles?subscription=PREMIUM" \
  -H "X-Tenant-Id: $TENANT"

# Admin count (requires global-admin)
curl -s -u "global-admin:secret" http://localhost:8080/admin/dealers/countBySubscription \
  -H "X-Tenant-Id: $TENANT"

# Missing header → 400
curl -s -u "$AUTH" http://localhost:8080/dealers
```

---

## Design Decisions

### Why thread-local `TenantContext`?
Standard approach for request-scoped state in servlet environments. The filter
clears it in a `finally` block to prevent leakage across pooled threads.

### Why denormalize `tenant_id` on `vehicles`?
Avoids a join to `dealers` on the hottest read path. The FK still enforces
referential integrity. The trade-off is that `tenant_id` must be set correctly
on insert (enforced in `VehicleService`).

### 404 vs 403 on cross-tenant access
Rather than always returning 404 (which leaks nothing but is confusing for
developers), the service returns 403 when the resource exists in another tenant.
This is intentional — the caller has valid credentials but is accessing the
wrong scope. Adjust to always-404 if security requirements demand it.

### MapStruct for mapping
Compile-time, type-safe, zero-reflection overhead. The
`NullValuePropertyMappingStrategy.IGNORE` strategy enables the partial-update
(`PATCH`) pattern without custom code.

### In-memory users for authentication
Sufficient for the scope of this task. Replace `InMemoryUserDetailsManager`
with a `JdbcUserDetailsManager` or JWT filter for production.
