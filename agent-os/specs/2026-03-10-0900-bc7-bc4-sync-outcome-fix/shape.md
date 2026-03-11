# Shape Notes — BC7 → BC4 Sync Outcome Fix

## Problem

Two independent issues prevented BC4 from receiving BC7 sync outcomes:

1. **Wrong event class** — `SyncOutcomeHandler` imported `channelintegration.domain.events.SyncSucceeded/SyncFailed` (stubs). Spring's `ResolvableTypeProvider` uses the exact generic type to route `@EventListener`, so envelopes carrying `ingestion.domain.events.*` were silently dropped.

2. **Missing `integrationId`** — BC7's `SyncSucceeded` and `SyncFailed` records lacked `integrationId`. BC4 needs it to call `integrationService.recordSyncSuccess(integrationId)` and `integrationService.markBroken(integrationId, reason)`. The `SyncJob` aggregate held `integrationId` as a field but did not include it in events.

## Options Considered

**Option A — Canonical (chosen)**: Add `integrationId` to ingestion events, delete stubs, re-point handler.
- Clean: single source of truth for event shape.
- No indirection.

**Option B — Adapter**: Keep stubs, add a translator bean that listens for ingestion events and re-emits channelintegration events.
- More files, more complexity, confusing two event types for the same concept.
- Rejected.

## Design Decisions

- `integrationId` placed after `tenantId` in record field order (consistent with `SyncJob` field declaration order).
- `markStuck()` also updated — it emits `SyncFailed` and BC4 should react to stuck jobs by marking the integration broken.
- No API/persistence contract changes — events are in-process only.
