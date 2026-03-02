# BC4 Channel Integration — Implementation Plan

## Context

The backend modular monolith refactor is complete (branch `feature/modular-monolith-refactor-backend`, pushed). BC4 (`channel-integration`) was scaffolded with only `.gitkeep` files. This plan implements the full DDD design for BC4: the `IntegrationInstance` aggregate, all 7 command handlers, 10 domain events, state machine, repository, persistence layer, REST controller, cross-BC API contracts, and comprehensive unit tests.

BC4 manages the lifecycle and operational state of external channel integrations (e.g., Google Ads) for a tenant. One `IntegrationInstance` per `(tenant_id, channel)` pair. Credentials are referenced (not stored here) — they live in BC5 or an external vault. This bounded context is the orchestration hub: it receives governance approval from BC2, emits `SyncRequested` to BC7 (Ingestion), and receives `SyncSucceeded`/`SyncFailed` back from BC7 to update instance health.

Branch: **`feature/be-channel-integration-bc4`**

---

## Java Package

`com.derbysoft.click.modules.channelintegration` (folder: `modules/channelintegration/`)

---

## Domain Model

### State Machine

```
SetupRequired --[attachCredential]-----------> Active
Active        --[detachCredential]-----------> SetupRequired
Active        --[pause]---------------------> Paused
Paused        --[resume]--------------------> Active
Active        --[markBroken(reason)]---------> Broken
Broken        --[attachCredential (reattach)]-> Active
```

`runSyncNow()` is only permitted when `Active`; throws `DomainError.ValidationError` otherwise.

### Commands → Domain Events (7 → 10)

| Command | Guard | Event(s) emitted |
|---------|-------|-----------------|
| `createIntegrationInstance(tenantId, channel, schedule)` | BC2 governance check | `IntegrationCreated` |
| `attachCredential(id, credentialRef)` | status ≠ Active | `CredentialAttached` (+ `IntegrationRecovered` if was Broken) |
| `detachCredential(id)` | status = Active | `CredentialDetached` |
| `pause(id)` | status = Active | `IntegrationPaused` |
| `resume(id)` | status = Paused | `IntegrationResumed` |
| `updateSyncSchedule(id, schedule)` | any status | `SyncScheduleUpdated` |
| `runSyncNow(id)` | status = Active | `SyncRequested` |
| *(incoming from BC7)* `handleSyncSucceeded` | — | *(no new events; updates health)* |
| *(incoming from BC7)* `handleSyncFailed` | — | `IntegrationMarkedBroken` |

---

## Implementation Status

- [x] Task 1: Spec documentation
- [x] Task 2: Domain layer
- [x] Task 3: Application layer
- [x] Task 4: Infrastructure layer
- [x] Task 5: Flyway migration
- [x] Task 6: Interfaces + API contracts
- [x] Task 7: Unit tests
- [x] Task 8: BoundaryRulesTest update
