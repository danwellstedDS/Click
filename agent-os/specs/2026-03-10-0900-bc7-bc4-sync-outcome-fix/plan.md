# BC7 → BC4 Sync Outcome Contract Fix

## Context

BC4 (`SyncOutcomeHandler`) listens for `EventEnvelope<channelintegration.domain.events.SyncSucceeded/SyncFailed>`.
BC7 emits `EventEnvelope<ingestion.domain.events.SyncSucceeded/SyncFailed>`.

Spring's `ResolvableTypeProvider` routes `@EventListener` by the exact generic payload class — so BC4 silently received nothing from BC7. The channelintegration stub events were written with a Javadoc note: *"defined here as a stub until BC7 is implemented; it will move to BC7's api/events package at that time"*.

Additionally, BC7's emitted events were missing `integrationId`, which BC4 needs to call `integrationService.recordSyncSuccess(integrationId)` and `integrationService.markBroken(integrationId, reason)`. `SyncJob` carries `integrationId` as a field but did not include it in the events it emitted.

**Chosen approach — Option A (canonical, no adapter indirection):**
- Add `integrationId` to BC7's `ingestion.domain.events.SyncSucceeded` and `SyncFailed`
- Update `SyncJob.markSucceeded()`, `markFailed()`, and `markStuck()` to emit `integrationId`
- Re-point `SyncOutcomeHandler` to listen for the ingestion events
- Delete the channelintegration stub event classes

## Changes Made

| File | Change |
|------|--------|
| `ingestion/domain/events/SyncSucceeded.java` | Added `integrationId` field |
| `ingestion/domain/events/SyncFailed.java` | Added `integrationId` field |
| `ingestion/domain/aggregates/SyncJob.java` | Pass `integrationId` when emitting events (markSucceeded, markFailed, markStuck) |
| `channelintegration/application/handlers/SyncOutcomeHandler.java` | Re-imported ingestion events; updated log fields to use `jobId` |
| `channelintegration/domain/events/SyncSucceeded.java` | Deleted (stub no longer needed) |
| `channelintegration/domain/events/SyncFailed.java` | Deleted (stub no longer needed) |
| `ingestion/domain/SyncJobTest.java` | Added `integrationId` assertions on emitted events |
| `docs/todo.md` | Marked item #6 done |

## Verification

```bash
# Build and confirm no compile errors:
docker compose build api

# Trigger a sync job and confirm BC4 health update fires:
# 1. POST /api/sync-jobs/trigger (or equivalent)
# 2. Check logs for "Sync succeeded for integration ..."
# 3. Check integration instance status transitions to HEALTHY
```

```sql
-- Confirm integration health updated after sync:
SELECT id, health_status, last_sync_at FROM integration_instances
WHERE id = '<integration-id>';
```
