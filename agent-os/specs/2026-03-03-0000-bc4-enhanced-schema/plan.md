# BC4 Channel Integration — Enhanced Schema & Cadence Plan

## Context

The `integration_instances` schema is minimal and doesn't match the BD spec. `SyncSchedule` can only express cron schedules — there's no way to represent "manual only" or interval-based syncing. Outcome data (`last_sync_at`, failure counts) is in-memory only via `IntegrationHealth`, never persisted. The unique constraint is `(tenant_id, channel)` which blocks multi-connection use cases.

This spec brings the domain model and DB schema into alignment:

- **Cadence semantics**: `cadence_type` (MANUAL | CRON | INTERVAL) + `interval_minutes`; rename `sync_schedule_cron` → `cron_expression` (now nullable for MANUAL).
- **Connection key**: New `connection_key VARCHAR(100)` field; unique constraint changes to `(tenant_id, channel, connection_key)`.
- **Outcome tracking**: `last_sync_at`, `last_sync_status` (NEVER | SUCCESS | FAILED) persisted on the entity.
- **Failure diagnostics**: `last_error_code`, `last_error_message`, `consecutive_failures`, `status_reason`.
- **Auditing**: `credential_attached_at`, `updated_by` (actor UUID).
- **DB invariants**: CHECK constraints enforcing active↔credential, cadence↔schedule consistency.

Branch: **`feature/be-bc4-enhanced-schema`**

---

## Execution Order

### Task 1 — Save spec documentation ✅

### Task 2 — Domain layer ✅

#### 2a — New `CadenceType` enum
File: `valueobjects/CadenceType.java`

#### 2b — New `SyncStatus` enum
File: `valueobjects/SyncStatus.java`

#### 2c — Replace `SyncSchedule` record
File: `valueobjects/SyncSchedule.java` — cadence-aware with MANUAL/CRON/INTERVAL static factories.

#### 2d — Replace `IntegrationHealth` record
File: `valueobjects/IntegrationHealth.java` — full outcome fields, withSuccess/withFailure.

#### 2e — Update `IntegrationInstance` aggregate
Add `connectionKey`, `credentialAttachedAt`, `updatedBy`; health defaults to `empty()`.

### Task 3 — DB migration ✅
File: `V202603030001__enhance_integration_instances.sql`

### Task 4 — Infrastructure layer ✅

### Task 5 — Application layer ✅

### Task 6 — Interfaces layer ✅

### Task 7 — Seed migration ✅
File: `V202603030002__seed_integration_instances.sql`

### Task 8 — Unit tests ✅
