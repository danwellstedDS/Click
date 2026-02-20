# Authentication System — Implementation Plan

## Context

This is the first real feature on a greenfield Kotlin/Ktor + React/TypeScript monorepo. The skeleton has two health routes, one domain class, Flyway/Exposed wired up, and no auth, routing infrastructure, or tests.

The auth system is foundational: it must establish multi-tenancy from day one. A key product requirement is that **internal users can belong to multiple tenants** and switch between them. This drives the schema design (global users + tenant_memberships junction table, not per-tenant user rows).

**Outcome:** Email/password login, JWT access tokens (8h) + refresh tokens (7d), multi-tenant user support with tenant switching, role-based access (ADMIN/VIEWER per tenant), and a `/login` page in the React frontend.

---

## Spec Folder

`agent-os/specs/2026-02-20-auth-system/`

---

## Critical Files

- `gradle/libs.versions.toml` — add all new library versions first
- `apps/api/build.gradle.kts` — add ktor-auth, ktor-auth-jwt, bcrypt dependencies
- `libs/persistence/build.gradle.kts` — add exposed-java-time, flyway-postgres, testcontainers
- `apps/api/src/main/kotlin/api/Main.kt` — wiring point for all plugins + repositories
- `libs/persistence/src/main/kotlin/persistence/Db.kt` — Flyway path is `filesystem:infra/db/migrations`
- `infra/db/migrations/` — new migrations sit alongside V1__init.sql
- `apps/web/src/main.tsx` — wrap app in AuthProvider

---

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-02-20-auth-system/` with plan.md, shape.md, standards.md, references.md.

---

## Task 2: Add Dependencies

**`gradle/libs.versions.toml` additions:**

```toml
[versions]
bcrypt = "0.10.2"
testcontainers = "1.20.1"

[libraries]
ktor-server-auth          = { module = "io.ktor:ktor-server-auth-jvm",           version.ref = "ktor" }
ktor-server-auth-jwt      = { module = "io.ktor:ktor-server-auth-jwt-jvm",       version.ref = "ktor" }
ktor-server-test-host     = { module = "io.ktor:ktor-server-test-host-jvm",      version.ref = "ktor" }
bcrypt                    = { module = "at.favre.lib:bcrypt",                     version.ref = "bcrypt" }
exposed-java-time         = { module = "org.jetbrains.exposed:exposed-java-time", version.ref = "exposed" }
flyway-postgres           = { module = "org.flywaydb:flyway-database-postgresql", version.ref = "flyway" }
testcontainers-junit      = { module = "org.testcontainers:junit-jupiter",        version.ref = "testcontainers" }
testcontainers-pg         = { module = "org.testcontainers:postgresql",           version.ref = "testcontainers" }
kotlin-test               = { module = "org.jetbrains.kotlin:kotlin-test",        version.ref = "kotlin" }
```

---

## Task 3: Domain Models

All pure Kotlin data classes in `libs/domain/src/main/kotlin/domain/`.

---

## Task 4: Database Migrations

Files in `infra/db/migrations/`. Naming: `V{YYYYMMDDHHMM}__description.sql`.

Four new migrations: tenants, users, tenant_memberships, refresh_tokens.

---

## Task 5: Persistence Layer

Table objects + port interfaces + concrete implementations in `libs/persistence/`.

---

## Task 6: Backend Auth Routes + Infrastructure

Plugins, routes, and updated Main.kt in `apps/api/`.

Routes:
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/refresh`
- POST `/api/v1/auth/switch-tenant` (authenticated)
- GET `/api/v1/auth/me` (authenticated)
- POST `/api/v1/auth/logout` (authenticated)

---

## Task 7: Frontend Auth Feature

`apps/web/src/features/auth/` + `apps/web/src/lib/apiClient.ts`

---

## Task 8: Tests

Unit, integration (Testcontainers), contract (Ktor testApplication), E2E placeholder.

---

## Verification

1. Start Docker DB: `docker compose up -d`
2. `./gradlew :apps:api:run` — verify Flyway runs 4 new migrations cleanly
3. `POST /api/v1/auth/login` with seeded user → 200, two httpOnly cookies set
4. `GET /api/v1/auth/me` → 200 with user profile
5. `POST /api/v1/auth/switch-tenant` → 200, new JWT with different tenantId
6. `POST /api/v1/auth/refresh` → 200, new access token cookie
7. `POST /api/v1/auth/logout` → 200, cookies cleared
8. `./gradlew test` — all tests pass
9. `npm run dev` in `apps/web/` → `/login` renders, login works

---

## Open Items (Non-Blocking)

- Seeding a test user: add a dev-only seed script or document how to INSERT manually
- Logback JSON encoder config — defer to observability task
- Refresh token rotation: rotates on every refresh (new token, old deleted). Accept trade-off for MVP.
