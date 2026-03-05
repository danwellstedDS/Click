# BC7 Ingestion — Shaping Notes

## Problem
Need to pull Google Ads campaign metrics reliably, with idempotency, retry logic, and audit trail.

## Key Decisions

### Idempotency
- Natural key: `(integrationId, accountId, dateFrom, dateTo, reportType)`
- PENDING/RUNNING jobs are deduplicated via partial unique index on `sync_jobs`
- Completed/failed jobs allow re-run (new job with same key is permitted once prior is terminal)

### Lease Semantics
- `lease_expires_at = now + 60min` on job start
- Scheduler tick (every 30s) recovers expired leases
- Auto-requeue once if TRANSIENT, then STUCK

### Retry Policy
- Exponential backoff: `min(2^attempts * 60s, 1800s) + jitter(0-30s)`
- Max 5 attempts per job
- PERMANENT failures do not retry

### Raw Data Upsert
- ON CONFLICT (integration_id, account_id, campaign_id, report_date) DO UPDATE
- Latest snapshot always wins (last-write wins for same natural key)

### Incident Lifecycle
- Open on first failure
- Escalate at 3 consecutive failures
- Auto-close on success
- Re-open if failure recurs after auto-close

### Rate Limiting
- 3 manual/backfill/force-run triggers per tenant per hour
- Returns `retryAfterSeconds` indicating when slot reopens

### Auth Failures
- Classified as PERMANENT → no retry
- Publishes `AuthFailureDetected` for BC5 to mark connection BROKEN

### BC7 Integration Points
- Listens to `SyncRequested` from BC4 (via `@EventListener`)
- Reads Google Ads bindings via BC5's `GoogleAdsQueryPort`
- Raw data consumed by BC8 (Normalisation) via `RawSnapshotWritten` event

## Scope (MVP)
- Campaign performance metrics only (CAMPAIGN_PERFORMANCE report type)
- Daily rolling 3-day correction window (`today-3d` to `yesterday`)
- Global UTC 02:00 ingestion time (no per-tenant scheduling)
- No automated retention cleanup
- `findAllActiveConnections()` added to `GoogleAdsQueryPort` for scheduler enumeration
