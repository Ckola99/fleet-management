# Fleet Asset Management API

A production-quality REST API built with **Spring Boot 3.5** and **Spring Security 6** that allows fleet managers to register assets, assign them to operators, and track usage records over time.

---

## Tech Stack

- **Java 24** — Core language
- **Spring Boot 3.5** — Application framework
- **Spring Security 6 + JWT** — Authentication and authorisation
- **Spring Data JPA + Hibernate** — Database ORM
- **H2 (file-based)** — Embedded database with persistent storage
- **Lombok** — Boilerplate reduction
- **Maven** — Build tool

---

## A Note on Security Configuration

> **This project was built as a technical assessment submission. For this reason, the `application.properties` file has been left in the repository with the JWT secret key and database credentials visible. This was intentional — it allows the reviewer to clone the repo, run the project immediately, and interact with the pre-seeded database without any additional setup.**

In a production environment this would never be acceptable. The correct approach is to use environment variables:

```properties
# application.properties (safe to commit)
application.security.jwt.secret-key=${JWT_SECRET_KEY}
application.security.jwt.expiration=${JWT_EXPIRATION}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

```bash
# .env (never committed — added to .gitignore)
JWT_SECRET_KEY=your-generated-base64-key
JWT_EXPIRATION=86400000
DB_USERNAME=sa
DB_PASSWORD=yourpassword
```

The secret key itself should be a randomly generated Base64-encoded 256-bit string:

```bash
openssl rand -base64 32
```

---

## Getting Started

### Prerequisites

- Java 24 (or Java 21+)
- Maven (included via `mvnw`)
- No Docker required — the database is embedded

### Running the Application

```bash
# Clone the repository
git clone https://github.com/Ckola99/fleet-management.git
cd fleet-management

# Set your JAVA_HOME (adjust path to your Java installation)
export JAVA_HOME="C:/Program Files/Java/jdk-24"

# Run the application
./mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**

The H2 database file is committed to the repository at `data/fleetdb.mv.db`. When you run the project you will immediately see existing test data from previous runs — no seeding or setup required.

---

## Testing the API — VS Code REST Client

A `requests.http` file is included in the root of the project. This is the fastest way to test every endpoint without needing Postman or any external tool.

### Setup

1. Install the **REST Client** extension by Humao Ren in VS Code
2. Open `requests.http` from the project root
3. A **Send Request** button appears above each request — click it to execute
4. The response appears in a panel on the right

### Recommended test order

The file is structured to walk you through the full flow from scratch:

**Step 1 — Register users**
Run the register requests to create a FLEET_MANAGER and an OPERATOR account. You only need to do this once since the H2 database persists between restarts.

**Step 2 — Login**
Run the login request. Copy the `token` value from the response JSON.

**Step 3 — Paste your token**
Replace `<YOUR_TOKEN_HERE>` in any request you want to test. The file includes clear instructions at the top explaining this.

**Step 4 — Create an operator record**
Before assigning assets, create an operator entity. This is separate from the user account — the operator record is the fleet system's representation of the person who will be assigned assets.

**Step 5 — Create assets and test all endpoints**
Work through the file top to bottom. Each section is clearly labelled and explains what role is required and what the expected behaviour is.

**Step 6 — Test role restrictions**
Use the OPERATOR token on FLEET_MANAGER-only endpoints (like `DELETE /api/assets/{id}`) — you should receive a `403 Forbidden`.

**Step 7 — Test soft delete**
Delete an asset, then try to GET it by ID. You should receive a `404 Not Found` — the asset is deactivated but still exists in the database.

---

## Database Console

The H2 web console is enabled and accessible while the application is running. This lets you inspect all tables and run raw SQL queries directly against the database.

Access it at **http://localhost:8080/h2-console**

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:file:./data/fleetdb` |
| Username | `sa` |
| Password | *(leave blank)* |

Once connected, run these queries to inspect the seeded data:

```sql
SELECT * FROM _USER;
SELECT * FROM OPERATOR;
SELECT * FROM ASSET;
SELECT * FROM USAGE_RECORD;
```

You can also verify soft deletes — deleted assets will still appear in the `ASSET` table with `ACTIVE = FALSE`.

---

## Authentication

All endpoints except `/api/v1/auth/**` require a valid JWT Bearer token in the `Authorization` header.

Tokens expire after **24 hours**. After expiry you will receive a `403` and need to log in again to get a fresh token.

### Roles

| Role | What they can do |
|------|-----------------|
| `FLEET_MANAGER` | Register assets, update assets, delete (soft), assign assets to operators, create operators, view everything |
| `OPERATOR` | View assets, log usage records, view assets assigned to a specific operator |

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "Chris Manager",
  "email": "chris@fleet.com",
  "password": "password123",
  "role": "FLEET_MANAGER"
}
```

Valid roles: `FLEET_MANAGER`, `OPERATOR`

### Login

```http
POST /api/v1/auth/authenticate
Content-Type: application/json

{
  "email": "chris@fleet.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Use this token in every subsequent request:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## API Endpoints

### Assets

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/assets` | FLEET_MANAGER | Register a new asset |
| `GET` | `/api/assets` | Any | List all active assets (paginated, filterable) |
| `GET` | `/api/assets/{id}` | Any | Get a single asset with full detail |
| `PUT` | `/api/assets/{id}` | FLEET_MANAGER | Update asset details |
| `DELETE` | `/api/assets/{id}` | FLEET_MANAGER | Soft-delete (deactivate) an asset |
| `POST` | `/api/assets/{id}/assign` | FLEET_MANAGER | Assign an asset to an operator |
| `POST` | `/api/assets/{id}/usage` | Any | Log a usage record |
| `GET` | `/api/assets/{id}/usage` | Any | Get usage history (paginated) |

#### Create Asset

```http
POST /api/assets
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Truck 01",
  "type": "VEHICLE",
  "status": "ACTIVE"
}
```

**Asset Types:** `VEHICLE`, `EQUIPMENT`, `TRAILER`, `OTHER`

**Asset Statuses:** `ACTIVE`, `INACTIVE`, `UNDER_MAINTENANCE`, `DECOMMISSIONED`

#### List Assets with Filters

```http
GET /api/assets?status=ACTIVE&type=VEHICLE&page=0&size=10
Authorization: Bearer <token>
```

Both `status` and `type` are optional. Use them individually or together.

#### Assign Asset to Operator

```http
POST /api/assets/1/assign?operatorId=1
Authorization: Bearer <token>
```

The `operatorId` must match a valid operator record created via `POST /api/operators`.

#### Log Usage Record

```http
POST /api/assets/1/usage
Authorization: Bearer <token>
Content-Type: application/json

{
  "hours": 8.5,
  "mileage": 120.0,
  "notes": "Long haul delivery to Pretoria"
}
```

`notes` is optional. `hours` and `mileage` must be zero or positive.

#### Get Usage History

```http
GET /api/assets/1/usage?page=0&size=10
Authorization: Bearer <token>
```

Returns a paginated list of all usage records for that asset, ordered by insertion.

---

### Operators

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/operators` | FLEET_MANAGER | Create an operator record |
| `GET` | `/api/operators/{id}/assets` | Any | Get all assets currently assigned to an operator |

#### Create Operator

```http
POST /api/operators
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "John Operator",
  "email": "john@fleet.com"
}
```

Note: this creates an operator entity in the fleet system. It is separate from registering a user account via `/api/v1/auth/register`. A person may have both — a user account for logging in and an operator record for being assigned assets.

#### Get Assets by Operator

```http
GET /api/operators/1/assets
Authorization: Bearer <token>
```

Returns all active assets currently assigned to that operator.

---

## Pagination

All list endpoints support Spring's standard pagination parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number (zero-indexed) |
| `size` | `20` | Items per page |
| `sort` | — | Field and direction e.g. `name,asc` or `status,desc` |

Example — get the second page of 5 assets sorted by name:
```http
GET /api/assets?page=1&size=5&sort=name,asc
Authorization: Bearer <token>
```

Paginated responses include metadata:
```json
{
  "content": [...],
  "totalElements": 12,
  "totalPages": 3,
  "size": 5,
  "number": 1
}
```

---

## Soft Delete

`DELETE /api/assets/{id}` does not remove the row from the database. It sets the `active` flag to `false`. After deletion:

- `GET /api/assets` will not include the asset
- `GET /api/assets/{id}` will return `404`
- The raw data is still visible in the H2 console via `SELECT * FROM ASSET`

This preserves history and usage records linked to the asset.

---

## Running Tests

```bash
export JAVA_HOME="C:/Program Files/Java/jdk-24"
./mvnw.cmd test
```

Tests run against an in-memory H2 database (`jdbc:h2:mem:testdb`) — completely isolated from the file-based database used by the running application. Your seeded data is never affected by the test suite.

**31 tests** across 4 test classes:

| Test Class | Tests | What it covers |
|------------|-------|----------------|
| `AuthenticationControllerTest` | 5 | Register, login, duplicate email, wrong password, missing fields |
| `AssetControllerTest` | 12 | Full CRUD, role restrictions, filters, soft delete, 404 cases |
| `UsageRecordControllerTest` | 7 | Log usage, usage history, role access, non-existent asset |
| `OperatorControllerTest` | 6 | Create operator, get assets, role restrictions, 404 cases |

---

## Project Structure

```
src/main/java/com/christopher/fleet_management/
├── asset/
│   ├── Asset.java                    JPA entity
│   ├── AssetController.java          REST endpoints
│   ├── AssetService.java             Business logic
│   ├── AssetRepository.java          Database queries
│   ├── AssetDTO.java                 Data transfer object
│   ├── AssetMapper.java              Entity ↔ DTO conversion
│   ├── AssetType.java                Enum: VEHICLE, EQUIPMENT, TRAILER, OTHER
│   └── AssetStatus.java              Enum: ACTIVE, INACTIVE, UNDER_MAINTENANCE, DECOMMISSIONED
├── operator/
│   ├── Operator.java
│   ├── OperatorController.java
│   ├── OperatorService.java
│   ├── OperatorRepository.java
│   ├── OperatorDTO.java
│   └── OperatorMapper.java
├── usage/
│   ├── UsageRecord.java
│   ├── UsageRecordController.java
│   ├── UsageRecordService.java
│   ├── UsageRecordRepository.java
│   ├── UsageRecordDTO.java
│   └── UsageRecordMapper.java
├── auth/
│   ├── User.java                     UserDetails implementation
│   ├── Role.java                     Enum: FLEET_MANAGER, OPERATOR
│   ├── UserRepository.java
│   ├── AuthenticationController.java
│   ├── AuthenticationService.java
│   ├── RegisterRequest.java
│   ├── AuthenticationRequest.java
│   └── AuthenticationResponse.java
├── config/
│   ├── SecurityConfig.java           Filter chain and access rules
│   ├── ApplicationConfig.java        Auth provider, password encoder
│   ├── JwtService.java               Token generation and validation
│   └── JwtAuthenticationFilter.java  Per-request JWT check
└── exception/
    ├── GlobalExceptionHandler.java   Centralised error handling
    └── ResourceNotFoundException.java
```

---

## Test Data

The repository includes a pre-seeded H2 database at `data/fleetdb.mv.db` containing data from manual testing runs. When you clone and run the project the following data is immediately available:

- 2 registered users — 1 FLEET_MANAGER (`chris@fleet.com`) and 1 OPERATOR (`john@fleet.com`), both with password `password123`
- 1 operator record (John Operator)
- 2 assets — Truck 01 (VEHICLE) and Forklift 01 (EQUIPMENT), both ACTIVE
- 2 usage records logged against Truck 01

You can log in immediately using the credentials above and start making requests without registering first.

---

## HTTP Status Codes

| Code | When it occurs |
|------|---------------|
| `200 OK` | Successful GET or PUT |
| `201 Created` | Successful POST (register, create asset, log usage) |
| `204 No Content` | Successful DELETE |
| `400 Bad Request` | Validation failed (missing required fields, invalid format) |
| `401 Unauthorized` | Wrong password during login |
| `403 Forbidden` | Missing token, expired token, or insufficient role |
| `404 Not Found` | Resource does not exist or has been soft-deleted |
| `409 Conflict` | Duplicate email during registration |
| `500 Internal Server Error` | Unexpected server error |

---

## Environment Configuration

The following properties are set in `application.properties`:

| Property | Value | Notes |
|----------|-------|-------|
| `spring.datasource.url` | `jdbc:h2:file:./data/fleetdb` | File-based H2, persists between restarts |
| `spring.jpa.hibernate.ddl-auto` | `update` | Creates tables on first run, preserves data on restart |
| `spring.h2.console.enabled` | `true` | H2 web console available at `/h2-console` |
| `application.security.jwt.secret-key` | Base64 string | Exposed for assessment purposes only — see security note above |
| `application.security.jwt.expiration` | `86400000` | 24 hours in milliseconds |
