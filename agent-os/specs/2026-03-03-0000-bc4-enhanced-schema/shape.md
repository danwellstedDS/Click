# BC4 Enhanced Schema — Shaping Notes

## Date: 2026-03-03
## Author: Claude Code (claude-sonnet-4-6)

---

## Problem Statement

The initial BC4 implementation was scaffolded with a minimal schema that covered core lifecycle operations. The BD spec requires:

1. **Multi-connection support**: A tenant may have multiple integrations for the same channel (e.g., two Google Ads accounts). The current `UNIQUE(tenant_id, channel)` constraint blocks this.

2. **Cadence flexibility**: The scheduler stub only fires all Active instances every 60 seconds. Real integrations need MANUAL (no auto-trigger), INTERVAL (every N minutes), or CRON (standard cron expression) cadence.

3. **Persisted outcome data**: `IntegrationHealth` currently lives in memory only — data is lost on restart. `last_sync_at`, failure counts, and error codes must be persisted.

4. **Auditing**: Who attached the credential and when? `credential_attached_at` and `updated_by` are needed for audit trails.

---

## Key Decisions

### 1. `connection_key` defaults to `"default"`
For backwards compatibility and for the common case of one integration per tenant/channel, `connectionKey` defaults to `"default"`. The HTTP API accepts it as optional. The unique constraint changes to `(tenant_id, channel, connection_key)`.

### 2. `SyncSchedule` becomes cadence-aware
Three static factories:
- `SyncSchedule.manual(timezone)` — no auto-scheduling
- `SyncSchedule.cron(cronExpression, timezone)` — standard cron
- `SyncSchedule.interval(intervalMinutes, timezone)` — fixed interval

Validation is enforced in the compact constructor. `cronExpression` is null for MANUAL/INTERVAL; `intervalMinutes` is null for MANUAL/CRON.

### 3. `IntegrationHealth` fully replaces the old record
The new record holds `lastSyncAt`, `lastSyncStatus`, `lastErrorCode`, `lastErrorMessage`, `consecutiveFailures`, `statusReason`. Factory method `empty()` produces the initial state. `withSuccess()` and `withFailure()` produce updated copies.

### 4. `actorId` thread-through
`attachCredential` now takes `UUID actorId` to record who performed the action. This is passed from the controller via `UserPrincipal.userId()`. For non-HTTP paths (scheduler), a system actor UUID (`00000000-0000-0000-0000-000000000000`) is used.

### 5. ScheduleService becomes cadence-aware
- MANUAL integrations: never auto-triggered.
- INTERVAL: triggered if `now >= lastSyncAt + intervalMinutes`.
- CRON: uses `org.springframework.scheduling.support.CronExpression` (already on classpath via spring-context).

### 6. DB CHECK constraints enforce domain invariants at the DB level
- `chk_active_requires_credential`: Active status requires a credential
- `chk_cron_requires_expression`: CRON cadence requires non-null `cron_expression`
- `chk_manual_has_no_schedule`: MANUAL cadence requires null cron and null interval

---

## Out of Scope

- BC5 credential vault implementation
- BC7 ingestion triggering (contracts remain as stubs)
- Async event bus (still in-process)
- OAuth token refresh flows
