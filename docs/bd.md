
## Structure

This architecture currently includes 12 BCs.
BC1-BC4 are foundations, BC5-BC6 are channel execution, BC7-BC10 are the data/reporting pipeline, and BC11-BC12 are measurement and capital-management domains.

# BC1 — Identity & Access

### Ubiquitous language

**Actor, User, Membership, Role, Permission, ActorContext**

### Responsibility

Provides **identity** and **coarse authorization** primitives and issues an `ActorContext` for every application request.

### Aggregates

- **User** (root): identity, auth identifiers
- **TenantMembership** (root or child of User): tenantId + role assignments
- **RoleAssignment** (entity/value object, depending on model)

### Commands (examples)

- `Authenticate`
- `AddMembership`
- `AssignRole`

### Domain events (examples)

- `UserAuthenticated`
- `MembershipAdded`
- `RoleAssigned`

### Publishes / consumed by

- Publishes: `ActorContext` (as a _context object_, not a domain event)
- Consumed by: all other BCs at the application boundary

### Anti-corruption notes

Other BCs **must not** depend on identity storage models; they depend on `ActorContext`.

### Locked decisions (confirmed)

- Authorization model is RBAC-only in MVP; ABAC is deferred.
- Agency delegation is modeled through scoped memberships.

----------

# BC2 — Tenant Governance

### Ubiquitous language

**GovernanceMode, Policy, Guardrail, AllowedAction**

### Responsibility

Defines tenant operating model and **invariants** for “risky actions” (writes, credential changes, destructive operations). Serves as **policy oracle** for other BCs.

### Aggregates

- **TenantPolicy** (root): governance mode, enabled channels, action rules

### Domain services

- **PolicyCheckService**: evaluates `PolicyDecision(action, actor, scope) -> Allow/Deny + reason`

### Commands

- `SetGovernanceMode`
- `EnableChannel`
- `UpdatePolicyRule`

### Domain events

- `GovernanceModeChanged`
- `ChannelEnabled`
- `PolicyUpdated`

### Integrations

- Consumed by BC4/BC6 as a synchronous policy check (application service call).
- No direct dependency on provider specifics (Google).

### Locked decisions (confirmed)

- OrgNode-specific policy overrides are allowed in MVP, but only for stricter constraints (never more permissive than tenant-global policy).
- Hard-block actions: auth/security/credential-sensitive operations.
- Override-eligible actions: operational workflows (for example manual operations, pacing/budget operations) with audit.

----------

# BC3 — Organisation Structure

### Ubiquitous language

**OrgNode, PropertyGroup, Hotel, Hierarchy**

### Responsibility

Source of truth for **org identity** (OrgNodeId) and hierarchy used for rollups and ownership boundaries.

### Aggregates

- **OrgTree** (root) _or_ **PropertyGroup** (root with recursive child groups)
- **Hotel** (root): leaf node identity and hotel metadata

(Implementation can vary; DDD point is ownership of the model and invariants.)

### Commands

- `CreateGroup`
- `AddHotel`
- `MoveOrgNode`
- `AssignHotelToGroup`

### Domain events

- `OrgNodeCreated`
- `OrgNodeMoved`
- `HotelCreated`
- `HierarchyChanged`

### Integrations

- BC5 references OrgNodeId as an external identity for bindings.
- BC10 uses hierarchy for read model aggregation.

### Locked decisions (confirmed)

- MVP node types remain `PropertyGroup` and `Property` only.
- Late binding uses unresolved-first semantics, then auto-bind on identity availability with audit event emission.

----------

# BC4 — Channel Integration (Generic Control Plane)

### Ubiquitous language

**IntegrationInstance, CredentialRef, IntegrationStatus, SyncSchedule, SyncRequest**

### Responsibility

Owns the **lifecycle** and **operational state** of a tenant’s channel integration:

- connected/paused/broken
- credential lifecycle reference (not provider shape)
- scheduling and manual sync triggers

### Aggregates

- **IntegrationInstance** (root): `(tenantId, channel)` identity, status, schedule, credentialRef
- **IntegrationCredential** _(optional separate aggregate)_: rotate/revoke, encrypted reference

### Domain services

- **IntegrationHealthService**: interpret failure outcomes into status transitions
- **ScheduleDueService**: determines “due for sync”

### Commands

- `CreateIntegrationInstance`
- `AttachCredential`
- `PauseIntegration` / `ResumeIntegration`
- `RunSyncNow`
- `UpdateSyncSchedule`

### Domain events

- `IntegrationCreated`
- `CredentialAttached`
- `IntegrationPaused` / `IntegrationResumed`
- `SyncRequested`
- `IntegrationMarkedBroken` / `IntegrationRecovered`

### Integrations

- Consults BC2 for guardrails on credential changes / triggers.
- Publishes `SyncRequested` consumed by BC7.
- Receives outcomes from BC7 (e.g., `SyncSucceeded`, `AuthFailed`) to update status.

### Key boundary

BC4 does **not** own provider account graphs, GAQL, campaign semantics, or per-account health.

### Locked decisions (confirmed)

- MVP supports exactly one integration instance per tenant/channel.
- Target SLA for ingest-failure reflection into integration status is <= 5 minutes.

----------

# BC5 — Google Ads Account & Access (Provider Scope + Bindings)

### At a glance

- **Purpose**: own Google Ads account access, tenant MCC connectivity, and authoritative account-to-org bindings.
- **Operating mode**: MCC-only, one MCC connection per tenant (hard invariant in MVP/Phase 1).
- **Downstream contract**: BC5 provides authoritative current-state resolved account scope for BC6/BC7/BC9/BC11 consumers.

### Ubiquitous language

**GoogleConnection, ManagerAccount (MCC), CustomerAccount, AccountGraph, AccountBinding, BindingHealth, ScopeResolution**

### Responsibility

- Manage MCC connection lifecycle and credential rotation.
- Discover and maintain latest account graph + account metadata.
- Own account-to-org bindings (`PropertyGroup` and `Property` targets).
- Resolve applicable accounts per property using hierarchy + union semantics.
- Validate access health and publish lifecycle events for operational consumers.
- Act as the sole authority for access/binding health state transitions.

### Locked decisions

- MCC-only connections.
- Exactly one MCC connection per tenant; second connection create is rejected.
- Credential replacement is in-place and marks effective bindings `STALE` immediately, then triggers manual discovery.
- Binding targets can be `PropertyGroup` or `Property`.
- Binding resolution is **union mode** across inherited and direct bindings.
- No `Primary` binding concept.
- Binding writes require account existence in latest discovered graph and valid active target node in BC3.
- Binding remove is soft (`REMOVED`) with audit/history retention.
- Resolved scope is current-state only (no as-of-time in MVP).
- Resolved views include `ACTIVE`, `BROKEN`, `STALE`; `REMOVED` is history-only.

### Aggregates

- **GoogleConnection** (root): `tenantId`, `mccAccountId`, credential ref, derived health, discovery schedule.
- **AccountBinding** (root): `customerAccountId -> orgNodeId`, optional qualifiers metadata, status, reason payload.
- **AccountGraphState** (entity under connection): latest discovered graph + diff/audit log references.

### Status and health model

- Binding statuses: `ACTIVE | BROKEN | STALE | REMOVED`.
- `STALE` threshold: `72h` without successful validation.
- Connection derived health:
- `HEALTHY`: effective bindings healthy.
- `DEGRADED`: mixed health or stale bindings.
- `BROKEN`: auth revoked/invalid, or no effective active bindings.

### Discovery model

- Scheduled + manual discovery.
- Default schedule: every `24h`, fixed local time per tenant connection.
- Manual discovery: single-connection trigger only in MVP.
- Degraded after `3` consecutive discovery failures.
- Auth/permission failures bypass threshold and break immediately.
- Recovery for broken bindings is automatic on successful revalidation.
- Broken bindings are skipped by BC7; stale bindings remain ingest-eligible.

### Authorization and audit

- Read access: `VIEWER+`.
- Write access: `ADMIN` and `SUPPORT`.
- Manual discovery restricted to write-capable roles.
- All writes are auditable (including manual discovery trigger).
- API/event reason payload includes both stable `reasonCode` and stable `reasonMessage`.

### Commands

- `CreateMccConnection`
- `RotateConnectionCredential`
- `DiscoverAccounts(connectionId)` (scheduled/manual)
- `CreateBinding(orgNodeId, customerAccountId, qualifiers?)`
- `RemoveBinding(bindingId)` (soft remove)
- `ResolveApplicableAccounts(propertyId)` (current-state authoritative query)
- `RecordAccessFailureObservation(sourceBc, context)` (observation only; BC5 decides state transitions)

### Domain events

- `ConnectionCreated`
- `ConnectionCredentialRotated`
- `ConnectionBroken`
- `ConnectionRecovered`
- `AccountGraphDiscovered`
- `AccountMetadataChanged`
- `BindingCreated`
- `BindingRemoved`
- `BindingBroken`
- `BindingRecovered`
- `BindingStaleThresholdExceeded`
- `BindingStaleRecovered`

### Integrations

- Uses BC4 integration credentials + operational coupling.
- Validates org scope against BC3 on binding writes and watches org changes for breakage.
- Publishes full lifecycle events to BC4 (at-least-once delivery).
- Consumes access-failure observations from BC6/BC7 and determines authoritative health transitions.
- BC6 consumes BC5 for scope validation.
- BC7 consumes BC5 for ingestion account enumeration (`BROKEN/REMOVED` excluded; `STALE` included).
- BC9/BC11 consume BC5 resolved scope and status/metadata transparency.

### Anti-corruption layer

A **Google Ads ACL** lives here (domain model <-> Google Ads API translations only).

### Locked decisions (confirmed)

- BC5 lifecycle events use a shared envelope contract with per-event `schemaVersion` and additive-only evolution.
- Binding FSM is explicit and constrained: `ACTIVE <-> BROKEN`, `ACTIVE <-> STALE`, `* -> REMOVED`; `REMOVED` is terminal (no direct recovery).
- Resolution dedupe is union by `customerAccountId`, with provenance retained for traceability.
- Manual discovery is asynchronous and returns a job token with completion event.
- Audit and account-graph diff retention is 13 months.

----------

# BC6 — Google Search Ads Management (Execution Objects)

### At a glance

- **Purpose**: own Click-side intended state and controlled execution of Google Search campaign structures.
- **MVP posture**: active with structural writes (campaign/ad group/ad/keyword), guarded by policy/scope checks and strong operator visibility.
- **Execution model**: async, revision-scoped, partial-apply with explicit item lifecycle.

### Ubiquitous language

**CampaignPlan, PlanRevision, PlanItem, WriteAction, ExecutionIncident, DriftReport, PublishGate**

### Responsibility

- Own Click-side intended state (`CampaignPlan`) as source-of-truth.
- Execute structural writes asynchronously to provider APIs.
- Enforce policy/scope guardrails at publish and execution time.
- Detect drift and provide actionable operator workflows.
- Emit rich execution telemetry for support/reporting consumers.

### Locked decisions

- BC6 is active in MVP.
- MVP includes structural writes: campaign/ad group/ad/keyword (deletes deferred to Phase 2).
- Intended-state model is authoritative (`CampaignPlan` source-of-truth).
- Publish gate required: draft edits execute only after explicit publish/apply.
- Two-stage validation: lightweight on save, full on publish/execution.
- Validation checks run both at publish and execution time against BC2/BC5.
- Apply is async via `WriteAction` queue.
- Partial apply is allowed with item-level status and targeted retries.
- Execution-time validation failures mark affected items `BLOCKED`, not whole-plan reject.
- Blocked items auto-retry once on dependency recovery, then manual retry.
- One revision apply at a time per tenant.
- Published plans are mutable via new draft revisions; active revision remains stable.
- Execution is strict revision-scoped (no cross-revision job mixing).
- Cancel behavior: cancel `QUEUED`; let `IN_PROGRESS` finish, then stop further actions.
- Deterministic apply order: Campaign -> AdGroup -> Ads -> Keywords (reverse for deletes).
- Drift handling in MVP: open incident + explicit operator action (no auto-reconcile).

### Phase 2 locked decisions

- `CampaignPlan` carries `targetCustomerId` (String) — the Google Ads customer account this plan targets.
- All 8 mutation types (CREATE/UPDATE for Campaign, AdGroup, Ad, Keyword) now call the real Google Ads API v23.
- Campaign create auto-creates a `CampaignBudget` resource in the same operation.
- Payload schema is JSON (see spec); all mutation clients parse via Jackson.
- `WriteAction.targetCustomerId` is snapshot-set at apply time from `CampaignPlan.targetCustomerId`.
- API version migration path: update imports in `GoogleAdsMutationClient` only; domain and port are unaffected.
- `MutationApiException` (carries `FailureClass`) is classified directly; `MutationAuthException` remains PERMANENT.

### Compliance fixes (2026-03-09)

Eleven gaps between spec and implementation closed. All are locked decisions:

- **Gap #1 — BC2 governance gate**: `PublishValidationService` and `WriteActionExecutor` now call `TenantGovernancePort.assertCanExecuteCampaigns(tenantId)` before publish/execution. Rejection throws `DomainError.Conflict(GOVERNANCE_BLOCKED)` or `MutationApiException(PERMANENT)` respectively.
- **Gap #2 — Binding validation at apply time**: `PlanApplyService` fails fast with `DomainError.Conflict` if no active binding exists for the tenant. No write actions are created for a plan with no reachable account.
- **Gap #3 — `AccessFailureObserved` cross-BC event**: `WriteActionExecutor` emits `AccessFailureObserved` on `MutationAuthException` before recording failure. BC5 (`AccessFailureObservedHandler`) subscribes and marks the connection broken.
- **Gap #4 — Composite incident identity**: `ExecutionIncident` identity is `(revisionId, itemId, failureClass)`. Migration `V202603090001` adds the three columns; repository finder updated.
- **Gap #5 — 24h recurrence window**: `ExecutionIncidentLifecycleService.onFailure` reopens `AUTO_CLOSED` incidents only if closure was < 24h ago; older closed incidents yield a fresh incident.
- **Gap #6 — APPLY counted in rate limit**: `ManualExecutionRateLimitService` counts `APPLY` triggers alongside `FORCE_RUN` and `RETRY`. `PlanApplyService` calls `checkOrThrow` at the start of `applyRevision`.
- **Gap #7 — `retryAfter` on rate-limit breach**: rate-limit exceeded throws `RateLimitExceededException(Duration)`. `CampaignPlanController` catches it and returns HTTP 429 with `Retry-After` header. `ApplyRevisionRequest.reason` is `@NotBlank`.
- **Gap #8 — Support contract fields in DTOs**: `PlanItemResponse` and `ExecutionIncidentResponse` now include `nextAction` (String) and `actionability` (String). Values computed from `failureClass + status` in the controller.
- **Gap #9 — Drift detection implementation**: `DriftDetectionService` queries BC7 snapshots via `SnapshotQueryPort` and compares key fields against the succeeded `WriteAction` payload. Returns `DriftItem` records per mismatched field.
- **Gap #10 — `ExecutionSummaryUpdated` emission**: `RevisionCompletionChecker` publishes `ExecutionSummaryUpdated` after marking a revision complete or failed.
- **Gap #11 — `EventEnvelope` contract**: `EventEnvelope` record extended with `correlationId`, `aggregateId`, `aggregateVersion`, `tenantId`, `payloadSchemaVersion`. `EventEnvelope.of()` factory defaults `aggregateVersion=0`, `payloadSchemaVersion="1.0"`, others null.

### Aggregates

- **CampaignPlan** (root): logical plan identity and high-level ownership context.
- **PlanRevision** (child root/process): immutable publishable revision of intended state.
- **PlanItem** (entity under revision): executable unit with explicit lifecycle state.
- **WriteAction** (root/process): queue/execution unit with idempotency and retry metadata.
- **ExecutionIncident** (read/process aggregate): failure and escalation lifecycle for support.
- **DriftReport** (read aggregate): detected intended-vs-provider differences with severity/actions.

### State model

- Item states: `DRAFT | PUBLISHED | QUEUED | IN_PROGRESS | SUCCEEDED | FAILED | BLOCKED | CANCELLED`.
- Plan-level status is derived from item states (not independently set).
- Incident states mirror BC7: `OPEN | AUTO_CLOSED | REOPENED | ESCALATED`.
- Invalid state transitions are hard-rejected and audited.

### Domain services

- **PublishValidationService**
- **ExecutionService** (provider mutations via ACL)
- **RetryPolicyEngine**
- **DependencyRecoveryHandler**
- **DriftDetectionService**
- **IncidentLifecycleService**

### Commands

- `CreateCampaignPlan`
- `SaveDraftRevision`
- `PublishPlanRevision`
- `ApplyPlanRevision`
- `ForceRunPlanItem` (ADMIN/SUPPORT only, reason required)
- `RetryPlanItem` (reason required)
- `CancelPlanRevision`
- `AcknowledgeExecutionEscalation` (ackReason required)
- `PauseCampaign`
- `UpdateBudget`

### Domain events

- Revision-level:
- `PlanRevisionPublished`
- `PlanRevisionApplyStarted`
- `PlanRevisionApplyCompleted`
- Item-level:
- `WriteActionQueued`
- `WriteActionStarted`
- `WriteActionSucceeded`
- `WriteActionFailed`
- `WriteActionBlocked`
- Incident/read-side:
- `ExecutionIncidentOpened`
- `ExecutionIncidentEscalated`
- `ExecutionIncidentAutoClosed`
- `ExecutionIncidentReopened`
- `ExecutionSummaryUpdated` (debounced)

### Integrations

- Policy oracle: BC2 (allow/deny)
- Scope oracle: BC5 (binding + permissions)
- Integration health/credentials: BC4 (resolved at execution time)
- Emits `AccessFailureObserved` to BC5; BC5 decides/access transitions and publishes authoritative lifecycle to BC4.
- Uses shared cross-BC `correlationId` compatible with BC7.
- Publishes `PlanRevisionApplyCompleted`; BC7 may subscribe and schedule follow-up sync per BC7 policy.

### Execution controls and resilience

- Per-item idempotency key pattern: `revisionId + itemId + actionType + targetVersion`.
- Retry attempts: `3` total.
- Retry backoff: exponential + jitter.
- Failure classification aligned with BC7:
- `Transient` -> auto-retry.
- `Permanent` -> manual action.
- Auth/permission failures are permanent for current run and signaled upstream.
- Manual execution-action cap: shared `3/hour` per tenant.
- Rate-limit breach: hard reject with `retryAfter`.
- Force-run counts toward same cap; emergency bypass deferred to Phase 2.
- Mandatory reason required for manual apply, force-run, manual retry, and manual acknowledgment.
- Delete ordering rule is Phase 2 planned behavior only (MVP delete execution is disabled).

### Support tooling contracts

- Dedicated `ExecutionIncident` read model (separate from BC7 incidents).
- Incident identity: `revisionId + itemId + failureClass`.
- Escalation after `3` consecutive failures; manual acknowledgment required with `ackReason`.
- Acknowledged escalations move to separate queue view.
- `nextAction` canonical codes are produced by BC6 (UI renders labels).
- `actionability` field is explicit (`READ_ONLY`, `ACTIONABLE_BY_SUPPORT`, `ACTIONABLE_BY_ADMIN`).
- Tenant-level queue view exposed (`pending/running/blocked/failed`).
- `DriftReport` exposed with severity mapping:
- `HIGH`: policy/scope violations or missing critical entities.
- `MEDIUM`: performance-impacting config mismatch.
- `LOW`: non-critical metadata drift.
- `HIGH` drift starts as `OPEN` (not immediate escalation).
- `explain` query available for failed/blocked items.

### Audit and event contracts

- Immutable audit trail includes all commands and all state transitions.
- Audit entries include before/after payload hashes, actor context, and correlation IDs.
- Shared event envelope with mandatory fields:
- `eventId`, `eventType`, `occurredAtUtc`, `correlationId`, `aggregateId`, `aggregateVersion`, `tenantId`, `actorContext?`, `payloadSchemaVersion`.
- Event evolution is additive-only.
- `ExecutionSummaryUpdated` emits on `30s` debounce per tenant (infra-configurable).

### Locked decisions (confirmed)

- Full revision/item/incident transition matrices are mandatory and must be codified in a spec appendix and test suite.
- Retention for execution audits/incidents/drift records is 13 months.
- Delete semantics in Phase 2 use soft-delete with prechecks; hard-delete is restricted to admin/maintenance workflow.

----------

# BC7 — Ingestion (Jobs + Raw Data)

### At a glance

- **Purpose**: run reliable scheduled/manual/backfill extraction from provider APIs and persist auditable raw data for downstream normalization.
- **MVP posture**: daily cadence, low-volume robust controls, explicit incident/read-side support.
- **Downstream contract**: BC7 is the source of sync outcomes and raw snapshot publication for BC8.

### Ubiquitous language

**SyncJob, RawSnapshot, IdempotencyKey, LookbackWindow, FailureClass, SyncIncident**

### Responsibility

- Schedule and orchestrate provider pulls.
- Enforce idempotency and single-active-job semantics per key.
- Classify failures and apply retry/escalation policies.
- Persist raw snapshots and job audit trail.
- Publish sync and incident signals for support and downstream BCs.

### Locked decisions

- Scheduled cadence in MVP: daily only.
- Default run time: `02:00` tenant local time.
- Global default schedule only in MVP (no per-tenant override).
- Incremental pull window: last `1 day`.
- Rolling correction lookback: last `3 days`.
- Backfill in MVP: manual, `ADMIN/SUPPORT` only, max `14 days`, audited.
- Backfill priority: same as daily jobs.
- One active job per idempotency key.
- Idempotency key: `integrationId + accountId + dateWindow + reportType`.
- Force-run allowed for `ADMIN/SUPPORT` with mandatory reason.
- Stuck-job timeout: `60m`; recovery is auto-requeue once, then escalate.
- Retry policy: `5` attempts, exponential backoff with jitter.
- Manual trigger cap: shared `3/hour` per tenant across manual sync/backfill/force-run.
- Rate-limit breach behavior: hard reject with `retryAfter`.
- Raw data strategy: upsert-overwrite at natural key/date grain; keep snapshot/job audit metadata.
- Auth/permission failures are non-retryable for current job and immediately notify BC4/BC5.
- **Dev seed data** (`V202603090002`): 4 weekly sync jobs, 4 snapshots, 90 campaign rows (3 campaigns × 30 days, 2026-02-06 to 2026-03-07), and 2 sync incidents seeded for the demo tenant to support BC8 normalisation development and testing.

### Failure and incident model

- Failure classes include:
- `Transient` (retry): timeout/network, `5xx`, `429`.
- `Permanent` (no retry): invalid query/schema, auth/permission `4xx` (except `429`).
- Incident identity: `idempotencyKey + failureClass`.
- Incident statuses: `OPEN | AUTO_CLOSED | REOPENED | ESCALATED`.
- Incidents auto-close on next successful run for same key.
- Incidents auto-reopen on recurrence within `24h`.
- Escalate after `3` consecutive failures for same key.
- `ESCALATED` requires manual acknowledgment by `ADMIN/SUPPORT` with mandatory `ackReason`.
- Acknowledged escalations move to a separate queue view.
- Snooze is deferred to Phase 2.
- Emergency cap-bypass override is deferred to Phase 2.

### Aggregates

- **SyncJob** (root): scope, trigger type, idempotency key, attempts, state, lease, timestamps.
- **RawSnapshot** (root): provider scope + window metadata, storage pointer, checksum/fingerprint.
- **SyncIncident** (read/process aggregate): failure class, lifecycle status, actionability metadata.

### Domain services

- **JobOrchestrator**
- **ProviderFetcher** (Google reporting fetcher via ACL)
- **RetryPolicyEngine**
- **IncidentLifecycleService**
- **SnapshotWriter**

### Commands

- `StartDailySync(integrationId)`
- `RunSyncJob(jobId)`
- `TriggerManualSync(scope, reason)`
- `TriggerBackfill(scope, dateRange, reason)`
- `ForceRun(scope, reason)` (ADMIN/SUPPORT only)
- `AcknowledgeEscalation(incidentId, ackReason)`

### Domain events

- `SyncStarted`
- `RawSnapshotWritten`
- `SyncSucceeded`
- `SyncFailed` (with failure classification)
- `SyncIncidentOpened`
- `SyncIncidentEscalated`
- `SyncIncidentAutoClosed`
- `SyncIncidentReopened`
- `ManualTriggerRateLimited`

### Integrations

- Consumes `SyncRequested` from BC4.
- Consults BC5 resolved scope to enumerate ingest accounts.
- Skips BC5 bindings in `BROKEN/REMOVED`; includes `STALE`.
- Calls provider fetcher ACL to pull rows.
- Publishes `RawSnapshotWritten` to BC8.
- Publishes success/failure outcomes to BC4.
- Emits `AccessFailureObserved` to BC5 on auth/permission failures; BC5 publishes authoritative lifecycle/health events to BC4.
- Exposes `SyncIncident` read model to app/support tooling with role-based actionability.

### Shared boundary contract (BC5/BC6/BC7)

- `AccessFailureObserved` is an observation contract from BC6/BC7 to BC5.
- BC6/BC7 never mutate access/binding health state directly.
- BC5 is the only BC that emits authoritative binding/connection health lifecycle events.

### Locked decisions (confirmed)

- `Sync*` and `Incident*` events follow the shared envelope contract with per-event `schemaVersion` and additive-only evolution.
- Raw upsert-overwrite uses a typed natural-key registry per report type; unknown report types are hard-rejected.
- Job ownership uses a 60-minute lease with 5-minute heartbeat and fenced ownership token.
- Auth/permission failures are emitted as standardized `AccessFailureObserved` to BC5 (deprecating divergent BC7-specific auth-failure event path).
- Retention: raw snapshots 90 days hot storage; incidents and audit metadata retained 13 months.

----------

# BC8 — Normalisation (Metrics Canon)

### At a glance

- **Purpose**: convert BC7 raw provider rows into a stable, provider-agnostic canonical metrics model for mapping and reporting.
- **MVP posture**: campaign-day grain for Google Ads ingestion, with additive schema evolution and deterministic replay behavior.
- **Downstream contract**: BC8 publishes immutable canonical batches that BC9 can consume idempotently.

### Ubiquitous language

**CanonicalFact, CanonicalBatch, FactGrain, MappingVersion, MetricDefinition, DimensionSet, ReconciliationKey**

### Responsibility

- Transform raw rows into canonical facts with normalized dimensions/metrics.
- Enforce canonical schema and mapping-version rules.
- Guarantee deterministic, idempotent normalization for replay/rebuild.
- Publish canonical batch outcomes and quality signals.

### Locked decisions

- MVP fact grain: **tenant + channel + account + campaign + date**.
- Provider source for MVP: Google Ads rows from BC7.
- Canonical facts are **append-by-batch** with immutable `batchId`; corrections are emitted as new batches, not in-place mutation.
- Reprocessing same input snapshot + same mapping version is idempotent and produces the same `canonicalBatchId`.
- Mapping logic is versioned (`mappingVersion`) and stored on each fact.
- Event evolution is additive-only; no breaking payload changes in-place.
- Currency for spend is stored in **micros** and decimal form; reporting currency conversion remains downstream (BC11/BC12 scope).
- Time is normalized to both `dateLocal` (tenant/account timezone semantics from BC7 scope) and `occurredAtUtc`.

### Canonical fact model (MVP)

**Dimensions**

- `tenantId`
- `channel` (`GOOGLE_ADS` in MVP)
- `integrationId`
- `customerAccountId`
- `campaignId`
- `campaignName` (nullable, informational)
- `reportDate` (local date at source grain)

**Metrics**

- `impressions`
- `clicks`
- `costMicros`
- `conversions` (provider-reported; canonical but not authoritative booking truth)

**Technical fields**

- `canonicalFactId`
- `canonicalBatchId`
- `sourceSnapshotId`
- `mappingVersion`
- `ingestedAtUtc`
- `reconciliationKey` (deterministic key for idempotency and dedupe)
- `qualityFlags` (array; empty when clean)

### Aggregates

- **CanonicalBatch** (root): input snapshot references, mapping version, status, row counts, quality counters, checksum/fingerprint.
- **CanonicalFact** (entity): normalized metric row at canonical grain.
- **MetricDefinition** (root/config): canonical metric catalog and validation rules (optional as persisted domain object in MVP).

### Invariants (non-negotiable)

- Every canonical fact must reference exactly one `sourceSnapshotId` and one `canonicalBatchId`.
- A `CanonicalBatch` is immutable after `Produced` state.
- `reconciliationKey` uniqueness is enforced within `(canonicalBatchId, reconciliationKey)`.
- Numeric metrics are non-negative unless explicitly marked as correction rows.
- Facts failing schema or semantic validation are quarantined; they are never silently dropped.

### Domain services

- **Normalizer**: row-level mapping from raw provider schema to canonical schema.
- **BatchAssembler**: builds deterministic batch identity and checksum.
- **QualityValidator**: schema/semantic validation and quarantine routing.
- **IdempotencyGuard**: prevents duplicate batch production for same input+version tuple.

### Commands

- `NormalizeSnapshot(snapshotId, mappingVersion?)`
- `RebuildCanonicalBatch(snapshotId, mappingVersion)` (admin/support operational command)
- `ReconcileCanonicalBatch(batchId)` (verification/recount command)

### Domain events

- `CanonicalBatchStarted`
- `CanonicalFactQuarantined`
- `CanonicalBatchProduced`
- `CanonicalBatchRebuilt`
- `CanonicalBatchFailed`

### Read models / queries

- `GetCanonicalBatch(batchId)`
- `ListCanonicalBatches(scope, timeRange, status)`
- `GetCanonicalFacts(batchId, page)`
- `GetNormalizationQualityReport(batchId)`

### Data contracts (minimum fields)

**CanonicalBatchProduced**

- `canonicalBatchId`
- `tenantId`
- `channel`
- `sourceSnapshotIds[]`
- `mappingVersion`
- `factCount`
- `quarantinedCount`
- `checksum`
- `occurredAtUtc`

**CanonicalFact**

- `canonicalFactId`
- `canonicalBatchId`
- `sourceSnapshotId`
- `tenantId`
- `channel`
- `integrationId`
- `customerAccountId`
- `campaignId`
- `campaignName` (nullable)
- `reportDate`
- `impressions`
- `clicks`
- `costMicros`
- `conversions`
- `mappingVersion`
- `reconciliationKey`
- `qualityFlags[]`
- `ingestedAtUtc`

### Integrations

- Consumes `RawSnapshotWritten` from BC7.
- Uses BC3/BC5 reference data only for validation hints (not ownership of mappings).
- Publishes `CanonicalBatchProduced` consumed by BC9.
- Publishes quality/failure events consumable by support workflows and BC10 coverage views.

### Failure handling

- Schema mismatch -> quarantine row + `CanonicalFactQuarantined`.
- Batch-level fatal validation error -> `CanonicalBatchFailed`.
- Infra/transient failure -> retry under BC7/worker policy; no partial publish of produced batch event.
- Replay of already-normalized snapshot/version -> return existing batch (idempotent success).

### MVP slices

- **MVP-1**: Google Ads campaign-day normalization (`impressions`, `clicks`, `costMicros`, `conversions`) with `CanonicalBatchProduced`.
- **MVP-2**: add ad-group and keyword grain extensions behind mapping-version bump.
- **MVP-3**: cross-channel normalization pack and compatibility adapters.

### Locked decisions (confirmed)

- BC8 persists both `costMicros` (authoritative) and decimal `costAmount` in MVP.
- `device` and `network` dimensions are added in MVP-2 behind mapping-version bump.

----------

# BC9 — Attribution & Mapping

### At a glance

- **Purpose**: attach BC8 canonical facts to Click org structure with deterministic, explainable mapping logic.
- **MVP posture**: binding-first mapping with controlled heuristics, confidence scoring, and auditable overrides.
- **Downstream contract**: BC9 emits mapped fact batches for BC10 rollups and quality visibility.

### Ubiquitous language

**MappingRuleSet, MappingResult, MappingConfidence, MappingOverride, MappingRun, ResolutionReason**

### Responsibility

- Resolve canonical facts to `OrgNodeId` scope (`Property`/`PropertyGroup`) using a strict precedence model.
- Assign confidence and explainability metadata per mapping decision.
- Manage manual overrides with full audit trail.
- Emit mapping quality signals for operational follow-up.

### Locked decisions

- Mapping precedence order is fixed:
  1. Active manual override.
  2. Explicit BC5 account binding resolution.
  3. Rule-based deterministic heuristics.
  4. Unresolved (no implicit forced assignment).
- BC5 remains authoritative for account->org explicit mappings; BC9 does not mutate BC5 binding state.
- Override scope can be by `customerAccountId + campaignId` (preferred) or `customerAccountId` fallback.
- Overrides are soft-removable and fully auditable (history retained).
- Confidence bands are standardized:
  - `HIGH`: explicit override or unambiguous explicit binding.
  - `MEDIUM`: deterministic rule match with single candidate.
  - `LOW`: ambiguous candidate set reduced by weak heuristics.
  - `UNRESOLVED`: no safe mapping.
- Mandatory review trigger: any `LOW` or `UNRESOLVED` result.
- Mapping is idempotent per `(canonicalBatchId, ruleSetVersion, overrideSetVersion)`.
- Re-runs produce new `MappingRun` records; prior outputs remain queryable for audit.

### Aggregates

- **MappingRuleSet** (root): versioned deterministic rules and precedence policy.
- **MappingOverride** (root): operator-set correction with scope, reason, actor, and lifecycle state.
- **MappingRun** (root/process): execution metadata, input versions, counts, and quality summary.
- **MappedFact** (entity): canonical fact + resolved org scope + confidence + reason code.

### Invariants (non-negotiable)

- Every `MappedFact` must reference exactly one source `canonicalFactId`.
- `UNRESOLVED` mappings cannot include a synthetic `OrgNodeId`.
- Rule set and override versions used in a run are immutable after run completion.
- Mapping output must be reproducible from stored inputs and versions.
- Override writes require write-capable roles (`ADMIN`/`SUPPORT`) and reason text.

### Domain services

- **Mapper**: resolves `CanonicalFact -> MappingResult`.
- **ConfidenceScorer**: computes confidence band and score from evidence strength.
- **OverrideResolver**: applies override precedence and lifecycle filtering.
- **AmbiguityDetector**: detects multi-candidate conflicts and emits actionable reason codes.

### Commands

- `MapCanonicalBatch(batchId)`
- `SetMappingOverride(scope, targetOrgNodeId, reason)`
- `RemoveMappingOverride(overrideId, reason)` (soft remove)
- `ReprocessMapping(batchId, ruleSetVersion?)`

### Domain events

- `MappingResultBatchProduced`
- `LowConfidenceMappingDetected`
- `MappingOverrideSet`
- `MappingOverrideRemoved`
- `MappingRunFailed`

### Read models / queries

- `GetMappingRun(runId)`
- `ListMappingRuns(scope, timeRange, status)`
- `GetMappedFacts(batchId, confidenceFilter?)`
- `GetLowConfidenceQueue(scope, timeRange)`
- `GetMappingExplain(factId)`

### Data contracts (minimum fields)

**MappingResultBatchProduced**

- `mappingRunId`
- `canonicalBatchId`
- `tenantId`
- `ruleSetVersion`
- `overrideSetVersion`
- `mappedCount`
- `lowConfidenceCount`
- `unresolvedCount`
- `occurredAtUtc`

**MappedFact**

- `mappedFactId`
- `mappingRunId`
- `canonicalFactId`
- `tenantId`
- `resolvedOrgNodeId` (nullable when unresolved)
- `resolvedScopeType` (`Property` | `PropertyGroup` | null)
- `confidenceBand` (`HIGH|MEDIUM|LOW|UNRESOLVED`)
- `confidenceScore` (0..1)
- `resolutionReasonCode`
- `ruleSetVersion`
- `overrideApplied` (boolean)
- `mappedAtUtc`

### Integrations

- Reads OrgNode identities from BC3 (by reference).
- Reads bindings from BC5 (explicit account->org mapping).
- Consumes `CanonicalBatchProduced` from BC8.
- Produces mapped facts consumed by BC10.
- Emits low-confidence/unresolved queue signals for support workflows.

### Failure handling

- Missing/invalid rule set -> `MappingRunFailed` (no partial publish as success).
- Reference lookup error (BC3/BC5 transient) -> retry run according to worker policy.
- Ambiguous resolution -> produce `LOW`/`UNRESOLVED` output, never hard-fail whole run unless systemic.
- Duplicate run request with same idempotency tuple -> return existing run result.

### MVP slices

- **MVP-1**: explicit binding-based mapping + manual overrides + low-confidence queue.
- **MVP-2**: deterministic naming heuristics and richer explainability payloads.
- **MVP-3**: channel-aware heuristic packs and confidence calibration tooling.

### Locked decisions (confirmed)

- End-user MVP UI shows `confidenceBand + reasonCode`; numeric `confidenceScore` is support/debug-facing.
- Overrides are unbounded in MVP; optional time-bounded overrides are a planned MVP-2 enhancement.

----------

# BC10 — Reporting & Portfolio Intelligence

### At a glance

- **Purpose**: provide decision-grade read models and portfolio views from mapped canonical performance data.
- **MVP posture**: projection-first read side with deterministic rebuild, strong freshness signals, and export-ready query APIs.
- **Downstream role**: primary consumer-facing analytics surface for performance, coverage, and operational health.

### Ubiquitous language

**Projection, Rollup, KPIView, CoverageView, PortfolioView, FreshnessState, QuerySlice**

### Responsibility

- Build and serve tenant-facing performance projections at Property and PropertyGroup scope.
- Provide portfolio rollups for channel/account/campaign slices.
- Surface mapping confidence and integration/binding health coverage.
- Expose query and export interfaces with predictable freshness semantics.

### Locked decisions

- BC10 is read-only; no write-back to BC8/BC9/BC4/BC5 domain state.
- Projection source of truth for performance is BC9 mapped outputs (not direct BC7 raw rows).
- Coverage transparency blends:
  - BC4 integration operational state
  - BC5 binding/access health
  - BC9 mapping confidence/unresolved rates
- MVP dashboards are fixed:
  1. Property performance rollup
  2. PropertyGroup portfolio rollup
  3. Coverage and health dashboard
  4. Low-confidence/unresolved mapping queue summary
- Freshness SLO in MVP:
  - Operational views: target <= 60 minutes end-to-end lag.
  - Executive rollups: target <= 24 hours.
- Rebuild strategy: incremental by batch/event for normal flow, full rebuild command for recovery.
- All projections carry `asOfUtc` and `sourceWatermark` for explainability.

### Aggregates

- **PropertyPerformanceView** (projection): KPI set at property grain.
- **PropertyGroupRollupView** (projection): aggregated KPI set at group grain.
- **PortfolioDistributionView** (projection): channel/account/campaign split metrics.
- **CoverageView** (projection): integration/binding/mapping quality state.
- **FreshnessView** (projection): lag/watermark by tenant and view type.

### Invariants (non-negotiable)

- Every projection row must be traceable to source batch/run identifiers.
- Projection rebuilds must be deterministic for same source inputs/version.
- Query APIs never return mixed watermark slices inside a single response without explicit per-slice watermark fields.
- KPI formulas are versioned and applied consistently across all scopes.

### Domain services

- **ProjectionBuilder**: event-driven projection updater.
- **RollupEngine**: multi-level aggregation and KPI derivation.
- **CoverageAssembler**: combines BC4/BC5/BC9 health signals.
- **QueryService**: paginated reads, filters, and exports.

### Commands

- `RebuildProjection(scope, from, to)` (internal/admin)
- `RefreshCoverageView(tenantId)` (internal)
- Query commands:
  - `GetPropertyReport(scope, timeRange, filters)`
  - `GetPropertyGroupRollup(scope, timeRange, filters)`
  - `GetPortfolioView(scope, timeRange, dims)`
  - `GetCoverageView(scope)`
  - `ExportPerformance(scope, timeRange, format)`

### Domain events

- Subscribes to:
  - `MappingResultBatchProduced` (BC9)
  - `LowConfidenceMappingDetected` (BC9)
  - BC4 integration lifecycle events
  - BC5 binding/connection lifecycle events
- Publishes:
  - `ProjectionUpdated`
  - `CoverageViewUpdated`
  - `ProjectionFreshnessBreached`

### Integrations

- Reads hierarchy from BC3 for rollups.
- Reads integration/binding health from BC4/BC5 for coverage.
- Reads mapped facts and confidence metadata from BC9.
- Serves reporting consumers and export clients.

### Data contracts (minimum fields)

**PropertyPerformanceView row**

- `tenantId`
- `propertyId`
- `timeWindow`
- `channel`
- `impressions`
- `clicks`
- `costMicros`
- `conversions`
- `ctr`
- `cpcMicros`
- `asOfUtc`
- `sourceWatermark`

**CoverageView row**

- `tenantId`
- `scopeType`
- `scopeId`
- `integrationStatus`
- `bindingHealthSummary`
- `mappedHighPercent`
- `mappedLowPercent`
- `unresolvedPercent`
- `staleBindingCount`
- `asOfUtc`

### Failure handling

- Projection update failure does not block upstream processing; event is retried from projection worker queue.
- Poison projection events are quarantined with alerting and replay tooling.
- If BC4/BC5 status feeds are delayed, coverage view is marked `STALE` with explicit freshness banner fields.
- Full rebuild is available as deterministic recovery path.

### MVP slices

- **MVP-1**: property/property-group rollups, basic KPI set, coverage dashboard, CSV export.
- **MVP-2**: richer portfolio slicing (campaign/account/channel), trend deltas, confidence drilldowns.
- **MVP-3**: alerting subscriptions and scheduled exports.

### Locked decisions (confirmed)

- Percentiles are computed on-demand in MVP; precomputation is deferred until performance evidence justifies it.
- Rollups use standard calendar periods in MVP; tenant-configurable fiscal calendars are planned for MVP-2.

----------



# BC11 — Measurement & Attribution

### At a glance

- **Purpose**: own the canonical, Click-controlled path from user intent to booking outcome across channels/properties.
- **Primary value**: consistent rollups, capital allocation inputs, and explainable attribution independent of platform-native attribution.
- **Downstream reliability goal**: produce stable outputs for Reporting, Budgeting, Optimisation, Governance, and Channel Connectors.

### Core outcomes

- A unified signal/event model for on-site actions and off-site clicks.
- A canonical conversion store with normalization for value/currency/timezone and support for multiple attribution models.
- Channel-agnostic performance summaries at PropertyGroup / Property / Campaign scopes.
- Reproducible attribution outputs with audit trail.

### Boundary

**Owns (authoritative)**

- Event taxonomy and schema versioning.
- Signal collection surfaces: web tag, redirect tracker, server hooks, optional platform import ingest.
- Identity stitching primitives inside Click scope (`clickId`, `sessionId`, `orderId` linkage).
- Attribution computation and result persistence.
- Revenue normalization and deduplication before rollups.
- Publication of attribution outputs and performance summaries.

**Does NOT own**

- Ad platform-side conversion configuration or platform-native attribution truth.
- On-site analytics UX products (for example GA4 reporting UX).
- Creative/campaign structure definitions (owned in campaign/channel operation domains).
- CRM/PMS source-of-truth semantics beyond integrated fields.

### External interfaces

**Inbound**

- Click Tag web events.
- Redirect click tracker events (metasearch + optional paid search).
- Server-to-server booking confirmations (booking engine / PMS / CRS).
- Optional platform conversion import payloads.

**Outbound**

- Reporting & Insights domain.
- Budgeting & Plans domain.
- Optimisation domain.
- Tenant Governance domain (policy/audit surfaces).
- Channel Connectors domain (conversion fan-out when enabled).

### Ubiquitous language

- **Signal**: raw tracking input.
- **Event**: normalized domain event.
- **ClickRef**: canonical reference to ad click context.
- **Conversion**: canonical booking outcome record.
- **AttributionResult**: conversion-to-touchpoint assignment under a specific model.
- **Model**: attribution algorithm version and parameters.
- **DedupKey**: deterministic duplicate-prevention key.

### Core capabilities

- **Collect**: ingest tag/redirect/server/platform signals.
- **Normalize**: validate schema, map event types, convert currency/timezone.
- **Stitch**: link `ClickRef <-> Session <-> Booking`.
- **Deduplicate**: merge conflicting/duplicate conversion records.
- **Attribute**: compute and persist model-specific outcomes.
- **Summarize**: materialize performance views by scope and time window.
- **Fan-out (optional MVP+)**: export conversions to ad platforms via Channel Connectors.

### Key invariants (non-negotiable)

- **Idempotency**: ingest is idempotent by `(source, sourceEventId)` or `dedupKey`.
- **Schema versioning**: every signal/event includes `schemaVersion`; incompatible events are rejected or quarantined.
- **Dedupe-before-summarize**: summaries only include deduplicated canonical conversions.
- **Currency normalization**: store both original and normalized amounts, including FX reference metadata.
- **Timezone correctness**: persist UTC timestamps and reporting-local timestamps with tenant/account timezone semantics.
- **Auditability**: every attribution result stores model version and input fingerprint for reproducibility.

### Primary aggregates (suggested)

- **TrackingSource**: source type, status, credentials, allowed domains, allowed schema versions.
- **ClickRef**: canonical click identity plus channel/platform hints.
- **SessionRef**: first-party session continuity for journey linking.
- **Conversion**: canonical booking outcome with lifecycle (`PROVISIONAL -> CONFIRMED -> ADJUSTED/CANCELLED`).
- **AttributionModel**: model definition/version/parameters.
- **AttributionResult**: per-conversion, per-model touchpoint weighting/outcome.
- **PerformanceSummary**: materialized rollups by dimension window.

### Commands (examples)

- `RegisterTrackingSource`
- `RotateTrackingKey`
- `IngestWebEvent`
- `IngestRedirectClick`
- `IngestServerBooking`
- `ImportPlatformConversion`
- `RecomputeAttribution(modelVersion, dateRange, scope)`
- `FinalizeConversion(bookingRef)`
- `InvalidateConversion(bookingRef, reason)`

### Domain events (published)

- `TrackingSourceRegistered`
- `ClickCaptured`
- `SessionStarted`
- `BookingCompleted` (canonical conversion created/updated)
- `ConversionDeduplicated`
- `AttributionComputed`
- `PerformanceSummaryUpdated`

### Read models / queries

- `GetAttribution(conversionId, modelVersion)`
- `GetPerformanceSummary(scope, dims, timeRange)`
- `ListConversions(scope, timeRange, status)`
- `GetSignalAuditTrail(conversionId)`

### Data contracts (minimum fields)

**CanonicalEvent**

- `eventId` (UUID)
- `eventType`
- `occurredAtUtc`
- `tenantId`
- `propertyGroupId`
- `propertyId` (nullable until resolved)
- `sessionId` (nullable)
- `clickRefId` (nullable)
- `source`
- `schemaVersion`
- `payload` (typed by event type)

**CanonicalConversion**

- `conversionId`
- `bookingRef` / `orderId`
- `valueOriginal`, `currencyOriginal`
- `valueNormalized`, `currencyNormalized`, `fxRateRef`
- `bookedAtUtc`, `bookedAtLocal`
- `status`, `dedupKey`
- links: `tenantId`, `propertyId`, `clickRefId`, `sessionId`

### MVP slices

- **MVP-1**: redirect ingest, server booking ingest (or CSV fallback), canonical conversion store + dedupe, last-click attribution, Property/PropertyGroup summaries.
- **MVP-2**: first-party web tag funnel events, stronger stitching, improved explainability/audit.
- **MVP-3**: connector fan-out to Google/Meta, multi-model attribution experimentation.

### Failure modes to design for

- Missing/blocked cookies.
- Booking engine integration constraints.
- Duplicate bookings arriving from multiple feeds.
- Late-binding where property is unknown at click time.

### Integrations with existing BCs

- **BC2 Tenant Governance**: tracking method allowlists, domain/privacy policy controls.
- **BC3 Organisation Structure**: property registry and group hierarchy for rollups and late binding.
- **BC4 Channel Integration**: operational status/scheduling guardrails for connector-facing exports.
- **BC5 Google Ads Account & Access**: platform IDs and account scope context when linking/fan-out is enabled.
- **BC10 Reporting & Portfolio Intelligence**: consumes `PerformanceSummaryUpdated` and explainability views.

### Non-goals

- Replacing GA4 as a general-purpose analytics suite.
- Building a cross-device identity graph beyond first-party scope.
- Becoming booking engine system-of-record.

### Locked decisions (confirmed)

- MVP-1 mandatory ingest paths include both webhook and file import fallback.
- MVP-1 includes a lightweight explicit reconciliation report versus ad-platform-reported conversions.

----------

# BC12 — Capital Allocation & Budgeting

### At a glance

- **Purpose**: own intentional allocation, governance, and enforcement of marketing capital across tenants, org scopes, channels, and strategies.
- **Decision basis**: budget pacing/performance uses canonical measurement from BC11, not platform-attributed revenue as source-of-truth.
- **Primary outcome**: controlled planning, pacing visibility, override governance, and future-ready reallocation.

### Architectural positioning

- **BC11 Measurement & Attribution**: provides canonical spend + revenue.
- **BC12 Capital Allocation & Budgeting**: decides how capital should be deployed.
- **Channel Connectors domain**: executes budgets in ad platforms (when enabled).

### Core responsibilities

- Define capital intent (`BudgetPlan`).
- Translate intent into enforceable `Budget` constraints.
- Monitor pacing versus canonical spend.
- Enforce soft guardrails.
- Record, validate, and audit overrides.
- Provide allocation and deviation signals for optimisation consumers.

### Ubiquitous language

- **BudgetPlan**
- **AllocationRule**
- **Budget**
- **Scope**
- **PacingState**
- **CapitalPolicy**
- **EnforcementLevel**
- **Override**
- **Deviation**

### Enforcement model (locked)

- **Default**: `EnforcementLevel = 2` (Soft Guardrails).
- Prevent budget increases above plan unless override is approved.
- Prevent new campaign launches that violate active allocation.
- Prevent capital shifts across allocation categories without approval.
- Require immutable override logging with actor attribution from BC1.
- Future path: tenant-selective `Level 3` automated reallocation.

### Core aggregates

**1) BudgetPlan (aggregate root)**

- Fields: `planId`, `tenantId`, `periodStart`, `periodEnd`, `totalCapital`, `currency`, `status (Draft|Active|Archived)`, `createdBy`, `createdAt`.
- Invariants: only one active `BudgetPlan` per tenant for overlapping periods; `sum(AllocationRules) <= totalCapital`; `currency` matches tenant base currency.

**2) AllocationRule (child of BudgetPlan)**

- Fields: `allocationId`, `planId`, `dimensionType (PropertyGroup|Property|Channel|Strategy)`, `dimensionId`, `allocationType (FixedAmount|PercentageOfParent)`, `value`.
- Invariants: percentage rules under the same parent scope sum to `<= 100%`; dimensions resolve to valid Org Structure scopes; circular scope allocations are invalid.

**3) Budget (operational constraint)**

- Fields: `budgetId`, `tenantId`, `scopeType (Property|Channel|Strategy|Campaign)`, `scopeId`, `periodStart`, `periodEnd`, `capAmount`, `enforcementLevel`, `sourcePlanId (nullable)`, `status (Active|Suspended)`.
- Invariants: budget resolves to exactly one valid scope; overlapping budgets for the same scope do not conflict; `enforcementLevel` does not exceed tenant `CapitalPolicy` maximum.

**4) CapitalPolicy (per tenant)**

- Fields: `tenantId`, `defaultEnforcementLevel (0-3)`, `toleranceThresholdPercent`, `overrideRoles`, `autoAdjustmentEnabled`, `deviationEscalationRules`.
- Invariant: `defaultEnforcementLevel <= systemMaxLevel`.

**5) BudgetOverride**

- Fields: `overrideId`, `budgetId`, `actorId`, `deltaAmount`, `reason`, `approvedBy (nullable)`, `createdAt`.
- Invariants: actor holds an allowed override role; overrides are immutable audit records.

### Pacing model (derived read model)

`PacingState` is computed from:

- **BC11**: `canonicalSpend`, `canonicalRevenue`.
- **BC12**: `capAmount`, budget period definition.

Fields:

- `scopeType`, `scopeId`, `period`, `capAmount`, `actualSpend`, `expectedSpend`, `deviationPercent`, `projectedEndSpend`, `status (OnTrack|AtRisk|Breach)`.

Invariant:

- Computation uses canonical spend as authoritative source.

### Commands (examples)

- `CreateBudgetPlan`
- `DefineAllocationRule`
- `ActivateBudgetPlan`
- `GenerateBudgetsFromPlan`
- `UpdateBudgetCap` (override required if increase exceeds allowed allocation)
- `RegisterOverride`
- `EvaluatePacing`
- `SuspendBudget`
- `EscalateDeviation`

### Domain events (published)

- `BudgetPlanCreated`
- `AllocationRuleDefined`
- `BudgetPlanActivated`
- `BudgetGenerated`
- `BudgetExceeded`
- `PacingDeviationDetected`
- `OverrideRegistered`
- `BudgetSuspended`
- `BudgetAdjusted`

### Read models / queries

- `GetActiveBudgetPlan(tenantId)`
- `GetCapitalAllocationBreakdown(planId)`
- `GetBudgetComplianceReport(scope, period)`
- `GetPacingState(scope, period)`
- `GetOverrideAuditTrail(budgetId)`
- `GetPortfolioCapitalDistribution(period)`

### MVP scope

- **Phase 1**: monthly `BudgetPlan`; allocations at Property + Channel; `EnforcementLevel=2`; canonical spend pacing; manual override for DMM roles; deviation + overspend projection dashboard.
- **Phase 2**: strategy-level allocations (for example Brand/Generic), cross-property marginal ROI comparison, advisory reallocation recommendations.
- **Phase 3**: auto-adjust within tolerance bands, cross-portfolio optimisation engine, scenario simulation (capital move analysis).

### Integrations with existing BCs

- **Depends on BC11 Measurement & Attribution**: canonical spend + revenue.
- **Depends on BC3 Organisation Structure**: scope resolution and hierarchy validation.
- **Depends on BC1 Identity & Access**: override role validation + actor attribution.
- **Depends on BC2 Tenant Governance**: allowed enforcement level policy by tenant.
- **Publishes to BC10 Reporting & Portfolio Intelligence**: pacing/compliance/projection read-side inputs.
- **Publishes to Optimisation domain**: budget constraint and deviation signals.
- **Publishes to Channel Connectors domain (future)**: budget push/adjust intent.

### Locked decisions (confirmed)

- BC12 owns capital approval workflow; BC2 enforces global governance constraints.
- Default deviation tolerance starts at 5% globally, with tenant-level overrides introduced in a later phase.


# Context map relationships (DDD-style)

- **BC1 → all**: Supplies `ActorContext` (shared context object)
- **BC2 (Governance)**: Upstream policy oracle for BC4/BC6 (synchronous call)
- **BC3 (Org Structure)**: Upstream identity/hierarchy for BC5/BC9/BC10
- **BC4 (Integration Ops)**: Publishes `SyncRequested` → BC7; receives outcomes from BC7
- **BC5 (Google Access)**: Upstream for BC6/BC7 scope decisions; publishes connection/binding lifecycle events to BC4
- **BC7 → BC8 → BC9 → BC10**: Event-driven pipeline (raw → canonical → mapped → projections)
- **BC11 (Measurement & Attribution)**: canonical signal + conversion + attribution domain feeding BC10 and downstream optimisation/planning consumers
- **BC12 (Capital Allocation & Budgeting)**: consumes canonical spend/revenue from BC11 to define/monitor/enforce budget intent; publishes pacing/compliance for BC10 and execution intent to channel connectors

----------
