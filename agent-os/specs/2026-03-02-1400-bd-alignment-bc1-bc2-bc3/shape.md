# Shape Notes — BD Alignment BC1/BC2/BC3

## Scope

Align BC1 (Identity & Access) and BC3 (Organisation Structure) domain models and terminology with the canonical bounded-context definitions in `docs/bd.md`.

## Key Decisions

### 1. PropertyGroup IS the tenant
`PropertyGroup` (table: `property_groups`) is the billing and operational unit. No separate `tenants` table is created. `TenantMembership.tenantId` is a direct FK to `property_groups.id`.

### 2. Data migration bridge
The existing `org_memberships.organization_id` stored the `organizations.id` that is also the `primary_org_id` of a `PropertyGroup`. The migration uses `property_groups.primary_org_id` as the bridge to re-point the FK to `property_groups.id`.

### 3. Role enum expansion
The 2-value `Role { ADMIN, VIEWER }` expands to `{ VIEWER, ANALYST, MANAGER, ADMIN, SUPPORT }`. Existing `ADMIN` and `VIEWER` values are preserved; new values extend the set. Default for new/migrated rows is `VIEWER`.

### 4. AuthClaims → ActorContext
`AuthClaims` is renamed to `ActorContext`. The fields are identical. This rename aligns with the UL (Ubiquitous Language) defined in the BD spec, where the term "ActorContext" describes the authenticated principal carrying user/tenant/role context.

### 5. OrgMembership → TenantMembership
`OrgMembership` with `organizationId` and `isOrgAdmin: boolean` is replaced by `TenantMembership` with `tenantId` (PropertyGroup.id) and `role: Role`. The two-step org→PropertyGroup lookup in `AuthCommandHandler` and `UserManagementHandler` is eliminated.

### 6. Domain events (BC1 and BC3)
Following the BC4 reference pattern (`IntegrationInstance`), BC1 and BC3 aggregates now emit domain events into a `List<Object> events` field. Events are published via `InProcessEventBus` from the application handler after `repository.save()`.

## Out of Scope
- BC2 (Tenant Governance) application layer — stub remains
- BC5–BC10 — unaffected
- UI / frontend changes
- CreateGroup / MoveOrgNode HTTP endpoints (events only)

## Risks
- Migration UPDATE step assumes `org_memberships.organization_id` = `property_groups.primary_org_id`. Confirmed by `UserManagementHandler.resolveOrgId()` which does exactly the reverse lookup.
- `switchTenant` in `AuthCommandHandler` previously looked up membership by `primaryOrgId`. After migration it looks up by `tenantId` directly via `TenantMembershipRepository.findByUserAndTenant()`.
