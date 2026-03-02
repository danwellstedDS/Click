
# BC1 — Identity & Access

### Ubiquitous language

**Actor, User, Membership, Role, Permission, ActorContext**

### Responsibility

Provides **identity** and **coarse authorization** primitives and issues an `ActorContext` for every application request.

### Aggregates

-   **User** (root): identity, auth identifiers
    
-   **TenantMembership** (root or child of User): tenantId + role assignments
    
-   **RoleAssignment** (entity/value object, depending on model)
    

### Commands (examples)

-   `Authenticate`
    
-   `AddMembership`
    
-   `AssignRole`
    

### Domain events (examples)

-   `UserAuthenticated`
    
-   `MembershipAdded`
    
-   `RoleAssigned`
    

### Publishes / consumed by

-   Publishes: `ActorContext` (as a _context object_, not a domain event)
    
-   Consumed by: all other BCs at the application boundary
    

### Anti-corruption notes

Other BCs **must not** depend on identity storage models; they depend on `ActorContext`.

----------

# BC2 — Tenant Governance

### Ubiquitous language

**GovernanceMode, Policy, Guardrail, AllowedAction**

### Responsibility

Defines tenant operating model and **invariants** for “risky actions” (writes, credential changes, destructive operations). Serves as **policy oracle** for other BCs.

### Aggregates

-   **TenantPolicy** (root): governance mode, enabled channels, action rules
    

### Domain services

-   **PolicyCheckService**: evaluates `PolicyDecision(action, actor, scope) -> Allow/Deny + reason`
    

### Commands

-   `SetGovernanceMode`
    
-   `EnableChannel`
    
-   `UpdatePolicyRule`
    

### Domain events

-   `GovernanceModeChanged`
    
-   `ChannelEnabled`
    
-   `PolicyUpdated`
    

### Integrations

-   Consumed by BC4/BC6 as a synchronous policy check (application service call).
    
-   No direct dependency on provider specifics (Google).
    

----------

# BC3 — Organisation Structure

### Ubiquitous language

**OrgNode, PropertyGroup, Hotel, Hierarchy**

### Responsibility

Source of truth for **org identity** (OrgNodeId) and hierarchy used for rollups and ownership boundaries.

### Aggregates

-   **OrgTree** (root) _or_ **PropertyGroup** (root with recursive child groups)
    
-   **Hotel** (root): leaf node identity and hotel metadata
    

(Implementation can vary; DDD point is ownership of the model and invariants.)

### Commands

-   `CreateGroup`
    
-   `AddHotel`
    
-   `MoveOrgNode`
    
-   `AssignHotelToGroup`
    

### Domain events

-   `OrgNodeCreated`
    
-   `OrgNodeMoved`
    
-   `HotelCreated`
    
-   `HierarchyChanged`
    

### Integrations

-   BC5 references OrgNodeId as an external identity for bindings.
    
-   BC10 uses hierarchy for read model aggregation.
    

----------

# BC4 — Channel Integration (Generic Control Plane)

### Ubiquitous language

**IntegrationInstance, CredentialRef, IntegrationStatus, SyncSchedule, SyncRequest**

### Responsibility

Owns the **lifecycle** and **operational state** of a tenant’s channel integration:

-   connected/paused/broken
    
-   credential lifecycle reference (not provider shape)
    
-   scheduling and manual sync triggers
    

### Aggregates

-   **IntegrationInstance** (root): `(tenantId, channel)` identity, status, schedule, credentialRef
    
-   **IntegrationCredential** _(optional separate aggregate)_: rotate/revoke, encrypted reference
    

### Domain services

-   **IntegrationHealthService**: interpret failure outcomes into status transitions
    
-   **ScheduleDueService**: determines “due for sync”
    

### Commands

-   `CreateIntegrationInstance`
    
-   `AttachCredential`
    
-   `PauseIntegration` / `ResumeIntegration`
    
-   `RunSyncNow`
    
-   `UpdateSyncSchedule`
    

### Domain events

-   `IntegrationCreated`
    
-   `CredentialAttached`
    
-   `IntegrationPaused` / `IntegrationResumed`
    
-   `SyncRequested`
    
-   `IntegrationMarkedBroken` / `IntegrationRecovered`
    

### Integrations

-   Consults BC2 for guardrails on credential changes / triggers.
    
-   Publishes `SyncRequested` consumed by BC7.
    
-   Receives outcomes from BC7 (e.g., `SyncSucceeded`, `AuthFailed`) to update status.
    

### Key boundary

BC4 does **not** own provider account graphs, GAQL, campaign semantics, or per-account health.

----------

# BC5 — Google Ads Account & Access (Provider Scope + Bindings)

### Ubiquitous language

**GoogleConnection, CustomerAccount, ManagerAccount (MCC), AccountGraph, Binding, Scope**

### Responsibility

Owns Google Ads-specific **access surface area** and how it is bound to OrgNodes:

-   discovered account graph
    
-   account settings
    
-   binding of customer accounts to org nodes
    
-   access validation and binding health
    

### Aggregates

-   **GoogleConnection** (root): consent state, integrationId reference, discovered graph version
    
-   **AccountBinding** (root): `orgNodeId -> customerAccountId` (+ status/health)
    
-   **AccountGraph** (entity/value object under connection): MCC/customer nodes and edges
    

### Domain services

-   **AccountDiscoveryService**: queries Google to refresh graph (uses credentials)
    
-   **BindingValidationService**: verifies access and marks binding broken/healthy
    

### Commands

-   `DiscoverAccounts`
    
-   `CreateBinding(orgNodeId, customerAccountId)`
    
-   `RemoveBinding`
    
-   `RefreshAccountSettings`
    

### Domain events

-   `AccountGraphDiscovered`
    
-   `BindingCreated`
    
-   `BindingBroken` / `BindingRecovered`
    

### Integrations

-   Reads credentialRef / resolved token via BC4 or a shared credentials component (through an ACL).
    
-   BC7 uses BC5 to determine which customer accounts to pull.
    
-   BC6 uses BC5 to validate write scope before mutate calls.
    

### Anti-corruption layer

A **Google Ads ACL** lives here (translating your domain needs to Google Ads API and back).

----------

# BC6 — Google Search Ads Management (Execution Objects)

### Ubiquitous language

**CampaignPlan, IntendedState, WriteAction, Reconciliation, Guardrails**

### Responsibility

Owns the domain for **creating/updating** Search campaign objects (if writes are enabled), including:

-   intended state representation
    
-   mutation command handling
    
-   auditability and idempotency
    
-   reconciliation of internal↔provider IDs
    

### Aggregates

-   **CampaignPlan** (root): intended campaign/ad group/ad/keyword state (as required)
    
-   **WriteAction** (root): durable record of a requested mutation (who/what/when), status lifecycle
    

### Domain services

-   **ExecutionService**: translates intended state/actions into provider mutations (via Google ACL)
    
-   **ReconciliationService**: resolves drift and updates linkages
    

### Commands

-   `CreateCampaignPlan`
    
-   `ApplyCampaignPlan`
    
-   `PauseCampaign`
    
-   `UpdateBudget`
    

### Domain events

-   `WriteActionQueued`
    
-   `WriteActionSucceeded`
    
-   `WriteActionFailed`
    
-   `PlanApplied`
    

### Integrations

-   Policy oracle: BC2 (allow/deny)
    
-   Scope oracle: BC5 (binding + permissions)
    
-   Integration health/credentials: BC4 (resolved at execution time)
    
-   Optional: trigger a pull refresh in BC7 to reconcile reporting.
    

----------

# BC7 — Ingestion (Jobs + Raw Data)

### Ubiquitous language

**SyncJob, Cursor, Retry, Snapshot, RawRow, Idempotency**

### Responsibility

Owns the **operational pipeline** for fetching provider data and storing **raw snapshots**:

-   job scheduling/queueing
    
-   retries, idempotency
    
-   raw storage as an auditable source
    
-   emits events to downstream canonicalization
    

### Aggregates

Often modeled as operational/process aggregates:

-   **SyncJob** (root): parameters, status, attempts, cursor/lookback
    
-   **RawSnapshot** (root): immutable-ish snapshot metadata + storage pointer
    

### Domain services

-   **JobOrchestrator**
    
-   **ProviderFetcher** (Google reporting fetcher is invoked via ACL)
    
-   **SnapshotWriter**
    

### Commands

-   `StartSync(integrationId)` (typically from event handler)
    
-   `RunJob(jobId)`
    
-   `RetryJob(jobId)`
    

### Domain events

-   `SyncStarted`
    
-   `RawSnapshotWritten`
    
-   `SyncSucceeded`
    
-   `SyncFailed` (with failure classification)
    

### Integrations

-   Consumes `SyncRequested` from BC4.
    
-   Consults BC5 to enumerate accounts and sync scope.
    
-   Calls Google fetcher (ACL) to pull rows.
    
-   Publishes `RawSnapshotWritten` to BC8.
    
-   Publishes success/failure outcomes for BC4 to update integration status.
    

----------

# BC8 — Normalisation (Metrics Canon)

### Ubiquitous language

**CanonicalFact, Dimension, MetricDefinition, CanonicalBatch**

### Responsibility

Transforms raw provider rows into a **provider-agnostic canonical fact model** (metrics/dimensions) used by reporting and portfolio logic.

### Aggregates

-   **MetricDefinition** (root) _(optional if you store definitions as domain data)_
    
-   **CanonicalBatch** (root): batch metadata + produced facts pointer
    
-   **CanonicalFact** (entity/table row; usually persisted in analytical store)
    

### Domain services

-   **Normalizer**: raw -> canonical transform, versioned mapping logic
    

### Commands

-   `NormalizeSnapshot(snapshotId)`
    

### Domain events

-   `CanonicalBatchProduced`
    

### Integrations

-   Consumes `RawSnapshotWritten` from BC7.
    
-   Publishes `CanonicalBatchProduced` consumed by BC9.
    

----------

# BC9 — Attribution & Mapping

### Ubiquitous language

**MappingRule, MappingResult, ConfidenceBand, Override**

### Responsibility

Attaches canonical facts to business structure (OrgNodes) using explicit bindings and optional heuristic rules.

### Aggregates

-   **MappingRuleSet** (root): explicit rules + optional naming conventions/heuristics
    
-   **MappingOverride** (root): manual corrections
    
-   **MappingRun** (root/process): batch mapping outcome metadata
    

### Domain services

-   **Mapper**: `CanonicalFact -> (OrgNodeId, ConfidenceBand)`
    
-   **ConfidenceScorer**
    

### Commands

-   `MapCanonicalBatch(batchId)`
    
-   `SetOverride`
    

### Domain events

-   `MappingResultBatchProduced`
    
-   `LowConfidenceMappingDetected`
    

### Integrations

-   Reads OrgNode identities from BC3 (by reference).
    
-   Reads bindings from BC5 (explicit account->org mapping).
    
-   Produces mapped facts consumed by BC10.
    

----------

# BC10 — Reporting & Portfolio Intelligence

### Ubiquitous language

**ReadModel, Rollup, Coverage, KPIView, PortfolioView**

### Responsibility

Owns **read models** and decision-oriented projections:

-   hotel rollups
    
-   group/chain rollups
    
-   efficiency KPIs
    
-   coverage and integration health views
    

(DDD note: this is typically a **CQRS read side** / projection context.)

### Aggregates

Often projection-focused rather than rich aggregates:

-   **HotelPerformanceView** (projection)
    
-   **GroupRollupView** (projection)
    
-   **CoverageView** (projection)
    

### Domain services

-   **ProjectionBuilder**: builds/refreshes views from mapped facts
    
-   **QueryService**: serves dashboards and exports
    

### Commands

-   `RebuildProjection` (internal)
    
-   queries like `GetHotelReport`, `GetGroupRollup`
    

### Domain events

-   Subscribes to `MappingResultBatchProduced` (BC9) and possibly `IntegrationStatusChanged` (BC4)
    

### Integrations

-   Reads hierarchy from BC3 for rollups.
    
-   Reads integration/binding health from BC4/BC5 for coverage.
    

----------

# Context map relationships (DDD-style)

-   **BC1 → all**: Supplies `ActorContext` (shared context object)
    
-   **BC2 (Governance)**: Upstream policy oracle for BC4/BC6 (synchronous call)
    
-   **BC3 (Org Structure)**: Upstream identity/hierarchy for BC5/BC9/BC10
    
-   **BC4 (Integration Ops)**: Publishes `SyncRequested` → BC7; receives outcomes from BC7
    
-   **BC5 (Google Access)**: Upstream for BC7 (scope enumeration) and BC6 (write scope validation)
    
-   **BC7 → BC8 → BC9 → BC10**: Event-driven pipeline (raw → canonical → mapped → projections)