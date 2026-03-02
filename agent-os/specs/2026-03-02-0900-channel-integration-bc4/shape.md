# BC4 Channel Integration — Shaping Notes

## Date: 2026-03-02
## Author: Claude Code (claude-sonnet-4-6)

---

## Scope

BC4 manages the lifecycle and operational state of external channel integrations (e.g., Google Ads, Meta Ads) for a tenant.

**In Scope:**
- `IntegrationInstance` aggregate with full state machine
- 7 command handlers, 10 domain events
- REST API for all operations
- Cross-BC governance check via BC2 API port
- Cross-BC sync triggering via event (to BC7)
- Persistence via JPA + Flyway migration
- Unit tests for domain, application, and controller layers
- ArchUnit boundary rules

**Out of Scope:**
- Actual credential storage (lives in BC5 or external vault)
- Full cron parsing/execution (stubbed as 60s polling for now)
- BC7 (Ingestion) implementation — only event contracts defined
- BC5 implementation — only a credential reference (UUID) is stored

---

## Key Decisions

### 1. One IntegrationInstance per (tenant_id, channel)
Enforced via DB unique constraint. Domain method `create()` does not need to check this — the DB constraint + repository will surface a conflict if violated. The application service should handle the uniqueness check before calling domain factory if needed, or let the DB constraint do it.

### 2. Status as Sealed Interface
`IntegrationStatus` is a sealed interface with 4 record implementations. Pattern matching with `instanceof` used for state machine guards. Stored as String in the database (name of the inner class).

### 3. Credential Reference Only
`CredentialRef` holds a `UUID credentialId` — a pointer to the actual credential in BC5/vault. BC4 never stores the secret itself.

### 4. Events List on Aggregate
The aggregate captures domain events in `List<Object> events`. The application service publishes them via `InProcessEventBus` and then calls `clearEvents()`. This follows the outbox pattern in-memory.

### 5. SyncSucceeded/SyncFailed Event Origin
Since BC7 is not yet implemented, `SyncSucceeded` and `SyncFailed` are defined in BC4's domain events as stubs. When BC7 is implemented, these will be moved to BC7's api/events and BC4 will import from there.

### 6. TenantGovernancePort Stub
BC2 (TenantGovernance) application layer is not yet built. A stub `@Bean` in `ModuleRegistry` always permits integration creation. This will be replaced when BC2 application layer is implemented.

### 7. ScheduleService is a Stub
Full cron evaluation deferred. Current implementation triggers `runSyncNow()` for all Active instances on a 60-second fixed-delay schedule. Proper cron parsing is a future milestone.

### 8. HTTP Auth
The controller does not use `@AuthenticationPrincipal` (unlike BC3's PropertyManagement) because integration management is tenant-scoped and the `tenantId` is passed in the request body/path. This avoids tying BC4 tightly to BC1's security model during scaffolding.

---

## Context

- BC1: Identity/Access — provides JWT auth
- BC2: TenantGovernance — provides governance approval (stub)
- BC3: OrganisationStructure — reference pattern for this implementation
- BC4: ChannelIntegration — this bounded context
- BC5: (not yet built) — will hold actual credentials
- BC7: Ingestion — will publish SyncSucceeded/SyncFailed events back to BC4
