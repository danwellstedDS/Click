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

### 6) ~~Fix BC4 <-> BC7 sync outcome mismatch~~ ✓ DONE
- [x] Align producer and consumer to one shared `SyncSucceeded` / `SyncFailed` contract.
- [x] Ensure BC4 health transitions consume the same event types BC7 publishes.

**Completed**: Added `integrationId` to `ingestion.domain.events.SyncSucceeded/SyncFailed`. Re-pointed `SyncOutcomeHandler` to consume ingestion events directly. Deleted channelintegration stub event classes. See `agent-os/specs/2026-03-10-0900-bc7-bc4-sync-outcome-fix/`.

### 7) ~~Unify access-failure observation path (BC7/BC6 -> BC5)~~ ✓ DONE
- [x] Standardize ingestion auth failure event into the shared BC5 observation contract.
- [x] Add BC5 consumer for the standardized ingestion observation.
- [x] Ensure BC5 remains sole authority for binding/connection health transitions.

**Completed**: Created canonical `googleadsmanagement.api.events.AccessFailureObserved`. BC6 (`WriteActionExecutor`) and BC7 (`JobExecutor`) both emit from this location. BC5 (`AccessFailureObservedHandler`) updated to import canonical event. Deleted `campaignexecution.domain.events.AccessFailureObserved` stub and `ingestion.domain.events.AuthFailureDetected`. See `agent-os/specs/2026-03-10-1000-bc7-bc5-auth-failure-canonical/`.

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

### 10) ~~Implement BC8 (Normalisation) MVP-1~~ ✓ DONE
- [x] Create module structure (domain/application/infrastructure/interfaces).
- [x] Implement `NormalizeSnapshot(snapshotId)` pipeline.
- [x] Publish `CanonicalBatchProduced`.
- [x] Define canonical fact persistence + versioned mapping strategy.
- [x] Idempotent batch production (deterministic batch ID, `IdempotencyGuard`).
- [x] Quality validation + quarantine path (`QualityFlag`, `CanonicalFactQuarantined`).
- [x] REST API: list batches, get batch, list facts, quality report.
- [x] Add `costAmount` generated column (DB computes `cost_micros / 1000000.0`; exposed on `CanonicalFactInfo` and `CanonicalFactData`).
- [x] Add BC8 test suite (CanonicalBatchTest, QualityValidatorTest, NormalizerTest, BatchAssemblerTest, IdempotencyGuardTest, NormalisationServiceTest). See `agent-os/specs/2026-03-10-1300-bc8-test-suite/`.

**Completed**: MVP-1 — Google Ads campaign-day normalisation. Migrations `V202603090003`–`V202603090004`. See `agent-os/specs/2026-03-09-1800-bc8-normalisation/`.

**costAmount gap closed**: Migration `V202603100001`. See `agent-os/specs/2026-03-10-1100-bc8-cost-amount/`.

**Deferred** (post-MVP-1): multi-channel dispatch, BC8 rebuild flow, BC9 consumer, per-tenant rate limiting.

### 11) ~~Implement BC9 (Attribution & Mapping) MVP-1~~ ✓ DONE
- [x] Implement mapping rule set + overrides + mapping run process.
- [x] Consume BC8 canonical batches via `CanonicalBatchProducedListener`.
- [x] Publish `MappingResultBatchProduced`, `MappingRunStarted`, `MappingRunFailed`, `MappingOverrideSet`, `MappingOverrideRemoved`.
- [x] Manual override CRUD (ACCOUNT + ACCOUNT_CAMPAIGN scopes).
- [x] Idempotency with override-set versioning.
- [x] REST API: list runs, get run, list facts, low-confidence queue, manage overrides.
- [x] Extend BC5 `account_bindings` with `org_node_id` + `org_scope_type`.

**Completed**: MVP-1 — explicit binding + manual overrides + low-confidence queue. Migrations `V202603090005`–`V202603090008`. See `agent-os/specs/2026-03-09-1900-bc9-attribution-mapping/`.

**LowConfidenceMappingDetected gap closed**: [x] Emit `LowConfidenceMappingDetected` (per-run, `lowConfidenceCount > 0 || unresolvedCount > 0`, includes fact IDs). See `agent-os/specs/2026-03-10-1200-bc9-low-confidence-event/`.

**Deferred** (post-MVP-1): MEDIUM/LOW heuristic resolution, bulk override import, re-attribution trigger, BC9 → BC10 consumer.

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

### 14) ~~Close BC8/BC9/BC10 MVP definitions~~ ✓ DONE
- [x] BC8 canonical metric definitions + minimum dimension grain.
- [x] BC9 confidence threshold + fallback mapping order.
- [x] BC10 MVP dashboard set + freshness SLA by view type.

**Completed**: BC8/BC9/BC10 sections in `docs/bd.md` now include locked decisions, invariants, commands/events, data contracts, failure handling, and MVP slices.

**Remaining follow-up**: implement BC9/BC10 modules and close residual BC8/BC9/BC10 open questions where marked.

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
