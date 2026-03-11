# Shaping Notes — BC7/BC6 → BC5 Auth-Failure Path

## Problem

Two bounded contexts emitting auth-failure signals with different event types:
- BC6 emits `campaignexecution.domain.events.AccessFailureObserved`
- BC7 emits `ingestion.domain.events.AuthFailureDetected` (different shape and package)

BC5's `AccessFailureObservedHandler` only handles `EventEnvelope<AccessFailureObserved>`. Spring's `ResolvableTypeProvider` matches by exact generic type parameter — so BC7's `AuthFailureDetected` envelope is silently dropped. Connection health is never marked broken from ingestion auth failures.

## Options Considered

**Option A** — Adapter in BC5: BC5 listens for both event types and normalises internally.
Risk: BC5 must know about BC7 internals. Coupling grows over time.

**Option B** — Canonical event owned by BC5 API layer: both BC6 and BC7 import from `googleadsmanagement.api.events`.
Chosen: clean contract, single source of truth, BC5 remains sole authority.

**Option C** — Shared kernel event in `sharedkernel.api.events`.
Rejected: shared kernel should carry only truly universal primitives; access failure is specific to Google Ads management.

## Constraints

- Event envelope name string `"AccessFailureObserved"` must match between producer and consumer. Both sides now use the same string.
- `AuthFailureDetected` had extra fields (`integrationId`, `occurredAt`) not needed by BC5. Dropped cleanly.
- No DB migration needed — this is a pure in-process event routing fix.
