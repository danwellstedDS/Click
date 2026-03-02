# BD Alignment — BC1, BC2, BC3 Refactoring Plan

## Context

The bounded-context definitions in `docs/bd.md` have drifted from the actual implementation across BC1 (Identity & Access), BC2 (Tenant Governance), and BC3 (Organisation Structure). The main problems are:

1. **BC1 uses BC2 vocabulary** — `OrgMembership` stores `organizationId` (an BC2 concept) instead of `tenantId = PropertyGroup.id` (the billing/ops unit).
2. **Role model is coarse** — `Role { ADMIN, VIEWER }` and `isOrgAdmin: boolean` don't match the BD-defined set or BC2's `GrantRole`.
3. **Wrong UL names** — `OrgMembership` → `TenantMembership`, `AuthClaims` → `ActorContext`.
4. **No domain events** in BC1 or BC3.
5. **BC3 missing commands** — No `CreateGroup` / `MoveOrgNode` handlers.

The user confirmed: keep **Property** (not Hotel) as the canonical product term — the codebase already reflects this. Tenant membership should reference **PropertyGroup.id** directly.

Branch: **`feature/be-bd-alignment-bc1-bc2-bc3`**

### Design Decision: No separate `tenants` table

`PropertyGroup` **is** the tenant. It is the billing/operational unit that a membership belongs to, and already has its own `property_groups` table. A separate `tenants` table would duplicate this concept. The migration in Task 3 simply re-points the FK in `tenant_memberships` from `organizations.id` (BC2) to `property_groups.id` (BC3), using `property_groups.primary_org_id` as the bridge for the data migration. After this change, BC1 has no runtime dependency on BC2 tables.

---

## Execution Order

### Task 1 — Save spec documentation

Create `agent-os/specs/2026-03-02-1400-bd-alignment-bc1-bc2-bc3/` containing:
- `plan.md` — this plan
- `shape.md` — shaping notes (scope, decisions, context)
- `standards.md` — content of backend/standards relevant to UL, DDD, events
- `references.md` — pointers to BC4 as the reference implementation

### Task 2 — BC1 Domain layer

**Path prefix:** `apps/api/src/main/java/com/derbysoft/click/modules/identityaccess/domain/`

#### 2a — Rename `OrgMembership` → `TenantMembership`

File: `entities/TenantMembership.java` (replaces `entities/OrgMembership.java`)
```java
public record TenantMembership(
    UUID id,
    UUID userId,
    UUID tenantId,       // ← was organizationId; now = PropertyGroup.id
    Role role,           // ← was isOrgAdmin: boolean
    Instant createdAt
) {}
```
Delete `entities/OrgMembership.java`.

#### 2b — Rename `AuthClaims` → `ActorContext`

File: `valueobjects/ActorContext.java` (replaces `valueobjects/AuthClaims.java`)
```java
public record ActorContext(
    UUID userId,
    UUID tenantId,
    String email,
    Role role
) {}
```
Delete `valueobjects/AuthClaims.java`.

#### 2c — Expand `Role` enum

File: `valueobjects/Role.java`
```java
public enum Role { VIEWER, ANALYST, MANAGER, ADMIN, SUPPORT }
```
(was `ADMIN, VIEWER`)

#### 2d — Add domain events

New directory `domain/events/`:
```java
// UserAuthenticated.java
record UserAuthenticated(UUID userId, String email, UUID tenantId, Role role, Instant occurredAt) {}

// MembershipAdded.java
record MembershipAdded(UUID membershipId, UUID userId, UUID tenantId, Role role, Instant occurredAt) {}

// RoleAssigned.java
record RoleAssigned(UUID membershipId, UUID userId, UUID tenantId, Role previousRole, Role newRole, Instant occurredAt) {}
```

#### 2e — Update `User` aggregate

File: `aggregates/User.java` — add `List<Object> events` capture; add:
- `static User register(...)` — emits no event at this stage (registration is auth-provider driven)
- `TenantMembership addMembership(UUID tenantId, Role role)` — emits `MembershipAdded`
- `void assignRole(UUID membershipId, Role newRole)` — emits `RoleAssigned`
- `getEvents()` / `clearEvents()`

### Task 3 — DB migration

Read existing migrations to determine next version `N`. Create `VN__align_bc1_tenant_membership.sql`:

```sql
-- Rename table
ALTER TABLE org_memberships RENAME TO tenant_memberships;

-- Rename column: organization_id → tenant_id
ALTER TABLE tenant_memberships RENAME COLUMN organization_id TO tenant_id;

-- Data migration: populate tenant_id from property_groups.primary_org_id
-- (org_memberships.organization_id was the primary_org_id of the property group)
UPDATE tenant_memberships tm
SET tenant_id = pg.id
FROM property_groups pg
WHERE pg.primary_org_id = tm.tenant_id;

-- Drop is_org_admin, add role
ALTER TABLE tenant_memberships DROP COLUMN is_org_admin;
ALTER TABLE tenant_memberships ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'VIEWER';

-- Restore FK constraint to property_groups
ALTER TABLE tenant_memberships
    ADD CONSTRAINT fk_tm_tenant FOREIGN KEY (tenant_id) REFERENCES property_groups(id);
```

### Task 4 — BC1 Application layer updates

**`AuthCommandHandler.java`** — simplify login flow:
- Remove `propertyGroupQueryPort.findInfoByPrimaryOrgId(...)` lookup (two-step gone)
- `TenantMembership` now holds `tenantId = PropertyGroup.id` directly
- Replace `isOrgAdmin` ternary with `membership.role()`
- Replace all `AuthClaims` references with `ActorContext`
- After login: publish `UserAuthenticated` event

**`UserManagementHandler.java`**:
- Remove `resolveOrgId()` helper (uses `principal.tenantId()` directly)
- Update `addMembership(...)` to pass `tenantId` (= PropertyGroup.id) and `Role` to aggregate
- Publish events from `user.getEvents()` / `user.clearEvents()`

**`JwtService.java`** (or equivalent token builder):
- Update claims population: replace `AuthClaims` with `ActorContext`

**`JwtAuthFilter.java`** (or equivalent filter):
- Replace `AuthClaims` with `ActorContext` in token parse/build

### Task 5 — BC3 Domain events

**Path prefix:** `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/domain/`

New directory `events/`:
```java
// OrgNodeCreated.java
record OrgNodeCreated(UUID propertyGroupId, String name, UUID parentId, Instant occurredAt) {}

// PropertyCreated.java
record PropertyCreated(UUID propertyId, UUID propertyGroupId, String name, Instant occurredAt) {}

// HierarchyChanged.java
record HierarchyChanged(UUID nodeId, UUID oldParentId, UUID newParentId, Instant occurredAt) {}
```

Update `PropertyGroup` aggregate (`aggregates/PropertyGroup.java`):
- Add `List<Object> events`
- Emit `OrgNodeCreated` from factory `create(...)` method
- Emit `HierarchyChanged` from `move(newParentId)` (add this command if missing)
- `getEvents()` / `clearEvents()`

Update `Property` aggregate (or entity) to emit `PropertyCreated` on construction.

Update `PropertyManagementHandler.java` to publish events via `InProcessEventBus` after save.

### Task 6 — Remove cross-BC coupling from BC1

**`PropertyGroupQueryPort.java`** (in BC3 api):
- Remove `findInfoByPrimaryOrgId(UUID orgId)` — no longer used by BC1 after Task 4
- Keep `findInfoById(UUID propertyGroupId)` and `isActive(UUID)`

**`ModuleRegistry.java`**:
- `AuthCommandHandler` constructor injection of `PropertyGroupQueryPort` — remove once simplified

Confirm no other callers of `findInfoByPrimaryOrgId` exist via `Grep`.

### Task 7 — BoundaryRulesTest update

File: `apps/api/src/test/java/com/derbysoft/click/architecture/BoundaryRulesTest.java`

Add or update:
```java
@Test
void identityAccessDomainShouldNotReferenceOrganisationStructureDomain() {
    noClasses().that().resideInAPackage("com.derbysoft.click.modules.identityaccess.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("com.derbysoft.click.modules.organisationstructure..")
        .check(classes);
}
```

Verify existing `identityAccessShouldOnlyAccessOrganisationStructureViaApi` still passes after removal of `findInfoByPrimaryOrgId` usage.

### Task 8 — Unit tests

**`TenantMembershipTest.java`** (new, replaces or updates `OrgMembershipTest` if any):
- `shouldHoldTenantIdDirectly` — verify `tenantId` is PropertyGroup.id, not Organization.id
- `shouldStorePrescribedRole`

**`UserAggregateTest.java`** (update):
- `shouldEmitMembershipAddedOnAddMembership`
- `shouldEmitRoleAssignedOnRoleChange`

**`AuthCommandHandlerTest.java`** (update):
- Remove tests for `findInfoByPrimaryOrgId` lookup
- `shouldBuildActorContextFromTenantMembership`
- `shouldPublishUserAuthenticatedEvent`

**`PropertyGroupAggregateTest.java`** (update):
- `shouldEmitOrgNodeCreatedOnCreate`
- `shouldEmitHierarchyChangedOnMove`
