# Standards Applied to Auth System

## api/contract

All API routes are versioned and tenant-scoped.

- Prefix all routes with `/api/v1`
- Every response is:
  - `{ success: true, data: ..., meta?: ... }` OR
  - `{ success: false, error: ..., meta?: ... }`
- Responses include `meta.requestId`
- Additive changes only within v1

### Success

```json
{ "success": true, "data": {}, "meta": { "requestId": "..." } }
```

### Error

```json
{ "success": false, "error": { "code": "VAL_001", "message": "..." }, "meta": { "requestId": "..." } }
```

---

## api/errors

Error codes are stable and grouped by prefix.

### Prefixes

- `VAL_` (400) validation
- `AUTH_` (401/403) auth/authz
- `RES_` (404) not found
- `CONFLICT_` (409) conflict
- `RATE_` (429) rate limit
- `UP_` (502/503) upstream provider failure
- `SYS_` (500) unexpected

### Rules

- Return safe messages only
- Log full error server-side with `requestId` and `tenantId`
- Always return both `code` and `message`

---

## auth/authorization-multitenancy

Multi-tenancy is enforced everywhere.

- `tenantId` is derived from token claims
- Never accept `tenantId` from request payload
- All tenant-owned tables include `tenant_id`
- All DB queries require `tenantId`
- Cross-tenant access returns `AUTH_403`

### Hierarchy

- Tenant → Chain → Hotel
- Chain belongs to one Tenant
- Hotel belongs to one Chain

---

## data/multitenancy

All tenant-owned entities include:

- `id` (UUID)
- `tenant_id`
- `created_at`
- `updated_at`

### Rules

- Composite uniqueness includes `tenant_id` where relevant
- Never allow reassignment across tenants via update
- Tenant isolation is mandatory in all repository methods

---

## data/flyway-migrations

Naming: `V{YYYYMMDDHHMM}__description.sql`

- Never edit applied migrations
- Every migration must include required indexes
- CI must run: fresh DB → migrate → start app

---

## observability/logging

Structured JSON logs only.

Each request log must include:

- `requestId`
- `tenantId`
- `route`
- `status`
- `durationMs`

Return `requestId` in API `meta`.

---

## frontend/architecture

Feature-based structure under `src/features`.

- No direct fetch inside components
- Central API client layer
- Strict TypeScript enabled

### Required pages

- `/login`
- `/`
- `/chains`
- `/hotels`
- `/reports`

---

## quality/testing

- Unit tests for domain logic
- Integration tests for repositories
- Contract tests for API endpoints
- One e2e smoke: login → create chain → pull report → view dashboard

### CI gates

- lint
- tests
- migrate fresh DB
- build
