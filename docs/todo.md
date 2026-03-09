# Architecture TODO Backlog

Source of truth for target design: `docs/bd.md`.

This backlog splits work into:
- **Spec TODOs**: decisions and contracts to lock.
- **Build TODOs**: implementation gaps in code.

---

## P0 (Critical) — Spec

### 1) Lock BC5/BC6/BC7 shared event contracts
- [ ] Define canonical payloads and schema versions for:
  - `Connection*`, `Binding*` (BC5)
  - `Sync*`, `SyncIncident*` (BC7)
  - access-failure observation BC6/BC7 -> BC5
- [ ] Define additive-only evolution policy and required envelope fields.
- [ ] Publish examples for producer + consumer validation tests.

**Done when**
- A single contract section in `bd.md` (or annex) defines all required fields, schema versions, and compatibility rules.

### 2) Close BC5 open design items
- [ ] Finalize binding finite-state machine (`ACTIVE/BROKEN/STALE/REMOVED`) transition table.
- [ ] Finalize scope-resolution dedupe rule when same account is inherited + direct.
- [ ] Finalize manual discovery API behavior (sync response vs async job token/event).
- [ ] Finalize retention period for account graph diff and binding audit history.

**Done when**
- No BC5 “Open questions” remain in `bd.md`.

### 3) Close BC6 open design items
- [ ] Finalize full revision/item/incident transition matrices.
- [ ] Finalize retention policy for execution audits/incidents/drift reports.
- [ ] Finalize phase-2 delete safety semantics.

**Done when**
- Transition matrix appendix exists and is testable against state machine rules.

### 4) Close BC7 open design items
- [ ] Finalize natural key rules per report type for raw upsert-overwrite.
- [ ] Finalize lease/heartbeat and crash recovery semantics.
- [ ] Finalize auth-failure signal payload contract to BC5/BC4.
- [ ] Finalize retention for raw snapshots/incidents/audits.

**Done when**
- Ingestion behavior is deterministic under retry, restart, and duplicate trigger scenarios.

---

## P0 (Critical) — Build

### 5) Replace BC2 governance stubs with real policy checks
- [ ] Implement a real `TenantGovernancePort` adapter (currently always permits).
- [ ] Enforce decision outcomes in BC4 and BC6 with explicit error codes.

**Current stub**
- `apps/api/src/main/java/com/derbysoft/click/bootstrap/di/ModuleRegistry.java`
- `apps/api/src/main/java/com/derbysoft/click/modules/tenantgovernance/api/ports/TenantGovernancePort.java`

### 6) Fix BC4 <-> BC7 sync outcome mismatch
- [ ] Align producer and consumer to one shared `SyncSucceeded` / `SyncFailed` contract.
- [ ] Ensure BC4 health transitions consume the same event types BC7 publishes.

**Current mismatch**
- BC4 expects `channelintegration.domain.events.Sync*`
- BC7 publishes `ingestion.domain.events.Sync*`

Files:
- `apps/api/src/main/java/com/derbysoft/click/modules/channelintegration/application/handlers/SyncOutcomeHandler.java`
- `apps/api/src/main/java/com/derbysoft/click/modules/ingestion/domain/events/SyncSucceeded.java`
- `apps/api/src/main/java/com/derbysoft/click/modules/ingestion/domain/events/SyncFailed.java`

### 7) Unify access-failure observation path (BC7/BC6 -> BC5)
- [ ] Standardize ingestion auth failure event into the shared BC5 observation contract.
- [ ] Add BC5 consumer for the standardized ingestion observation.
- [ ] Ensure BC5 remains sole authority for binding/connection health transitions.

**Current state**
- BC6 emits `AccessFailureObserved`.
- BC7 emits `AuthFailureDetected` (different shape/path).

### 8) Replace BC6 drift snapshot query stub
- [ ] Implement real `SnapshotQueryPort` adapter from BC7 snapshot store.
- [ ] Add integration tests for drift detection with real snapshots.

**Current stub**
- `apps/api/src/main/java/com/derbysoft/click/bootstrap/di/ModuleRegistry.java`

---

## P1 (High) — Build

### 9) Introduce OpenAPI-first API documentation
- [ ] Add OpenAPI generation for current REST surface (at minimum BC4/BC5/BC6/BC7 endpoints).
- [ ] Publish versioned OpenAPI artifact from CI (`openapi.json`/`openapi.yaml`).
- [ ] Ensure operationIds, request/response schemas, and error models are consistent.
- [ ] Generate and publish a Postman collection from OpenAPI as a build artifact.
- [ ] Add a short usage guide for importing/running the collection.

**Done when**
- A consumer can pull the latest OpenAPI spec and create/use a Postman collection without manual endpoint discovery.

### 10) Implement BC8 (Normalisation)
- [ ] Create module structure (domain/application/infrastructure/interfaces).
- [ ] Implement `NormalizeSnapshot(snapshotId)` pipeline.
- [ ] Publish `CanonicalBatchProduced`.
- [ ] Define canonical fact persistence + versioned mapping strategy.

**Current state**
- Placeholder only: `apps/api/src/main/java/com/derbysoft/click/modules/normalisation/.gitkeep`

### 11) Implement BC9 (Attribution & Mapping)
- [ ] Implement mapping rule set + overrides + mapping run process.
- [ ] Consume BC8 canonical batches.
- [ ] Publish `MappingResultBatchProduced` and low-confidence signals.

**Current state**
- Placeholder only: `apps/api/src/main/java/com/derbysoft/click/modules/attributionmapping/.gitkeep`

### 12) Implement BC10 (Reporting & Portfolio Intelligence)
- [ ] Build projection pipeline and query APIs for rollups/KPIs/coverage.
- [ ] Consume BC9 mapping outputs and BC4/BC5 health transparency.

**Current state**
- Placeholder only: `apps/api/src/main/java/com/derbysoft/click/modules/reportingportfolio/.gitkeep`

---

## P1 (High) — Spec

### 13) ~~Add BC7 mock data fixtures for downstream testing~~ ✓ DONE
- [x] Create deterministic mock Google Ads raw data fixtures at BC7 output shape (raw rows + snapshot metadata).
- [x] Include representative scenarios: normal steady-state daily data, auth/permanent failure-adjacent run metadata.
- [x] Provide seed/load commands for local and CI test runs.

**Completed**: `V202603090002__seed_bc7_mock_data.sql` — 4 sync jobs, 4 snapshots, 90 campaign rows (3 campaigns × 30 days), 2 incidents. See `agent-os/specs/2026-03-09-1700-bc7-mock-data-seed/`.

**Deferred** (out of MVP scope): late-arriving correction, duplicate/idempotent replay, multi-account scope, fixture versioning/changelog.

### 14) Close BC8/BC9/BC10 MVP definitions
- [ ] BC8 canonical metric definitions + minimum dimension grain.
- [ ] BC9 confidence threshold + fallback mapping order.
- [ ] BC10 MVP dashboard set + freshness SLA by view type.

---

## P2 (Planned) — Build + Spec

### 15) Implement BC11 (Measurement & Attribution)
- [ ] Build canonical signal ingest surfaces (redirect, server booking, optional tag/import).
- [ ] Build conversion store, dedupe, stitching primitives, attribution models, summaries.
- [ ] Define and enforce schema versioning/idempotency/audit invariants.

### 16) Implement BC12 (Capital Allocation & Budgeting)
- [ ] Build `BudgetPlan`, `AllocationRule`, `Budget`, `CapitalPolicy`, `BudgetOverride`.
- [ ] Build pacing read model using BC11 canonical spend/revenue.
- [ ] Implement soft-guardrail enforcement and override governance flow.

### 17) Close BC11/BC12 unresolved policy decisions
- [ ] BC11 MVP mandatory ingest path (webhook vs file vs both) + reconciliation requirement.
- [ ] BC12 ownership split for approvals (BC12 vs BC2) and default threshold standards.

---

## Cross-Cutting Quality Gates

### 18) Event contract test suite
- [ ] Producer/consumer contract tests for BC4/BC5/BC6/BC7.
- [ ] Replay/idempotency tests for at-least-once delivery.

### 19) Role/authorization matrix
- [ ] Single matrix mapping BC1 roles to BC4/BC5/BC6/BC7 write actions.
- [ ] Endpoint-level enforcement tests for all write-capable APIs.

### 20) Retention + observability baseline
- [ ] Standard retention policy document across audits/incidents/raw snapshots/graph diffs.
- [ ] Dashboards/alerts for stale bindings, escalated incidents, and failed sync/apply rates.
