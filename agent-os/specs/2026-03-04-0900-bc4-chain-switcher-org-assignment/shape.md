# BC4 Shaping Notes & Decisions

## Problem Shape

### Chain Context Switcher
The AppLayout header has a `<Dropdown menu={{ items: [] }}>` placeholder that currently shows the active tenant name but has no items. Users need to switch between chains they have access to.

**Appetite:** Small — one new backend endpoint (`GET /api/v1/auth/tenants`), one frontend context update, one AppLayout change.

**Solution Shape:**
- `GET /api/v1/auth/tenants` resolves membership UUIDs into `{ tenantId, tenantName, role }` tuples — same pattern as `me()`.
- `AuthContext` fetches and stores the list on mount.
- `AppLayout` maps the list to dropdown items; active tenant gets a `<CheckOutlined />` icon.
- Clicking an item calls the existing `switchTenant(tenantId)` which re-issues the JWT and refreshes `user`.

### Org Assignment on Chain Creation
When an admin creates a chain, they should optionally assign it to an organisation. This wires up:
1. `primary_org_id` on the `PropertyGroup` row.
2. An `AccessScope` (PROPERTY_GROUP type) pointing at the new chain.
3. A `ScopeAccessGrant` linking the org to the scope with ADMIN role.
4. A `TenantMembership` for the creator (always, regardless of org) so they can immediately switch to the new chain.

**Appetite:** Small-Medium — touches 4 backend files, creates 2 new backend files, touches 3 frontend files, creates 1 new frontend file.

---

## Decisions Made

### D1: `TenantMembership` for creator is always created
Even if no org is provided, the creator gets a membership. This allows them to switch to the new chain context immediately after creation. The org assignment is optional scaffolding on top.

### D2: `organizationId` is nullable in `CreateChainRequest`
No `@NotNull` validation. Chains without an org are valid (e.g., internal test chains).

### D3: `listTenants` goes on `AuthCommandHandler`, not a new service
Follows existing pattern — `me()` and `switchTenant()` are on the same handler. Adding `listTenants()` keeps the auth domain cohesive.

### D4: `OrganizationManagementController` is ADMIN-only
Follows the pattern established in `ChainManagementController`. The org list is only needed by admins creating chains — no reason to expose it more broadly.

### D5: `TenantSummary` is a new DTO, not reusing `TenantInfo`
`TenantInfo` (used in `LoginResponse`) only has `{ tenantId, role }`. The switcher needs `tenantName` too. A new `TenantSummary` record is clean and avoids retrofitting `TenantInfo`.

### D6: Frontend `tenants` list uses the new `TenantSummary` type, not `TenantMembership`
`TenantMembership` in `types.ts` is the login-response type without names. `TenantSummary` from `authApi.ts` includes the resolved name.

---

## Out of Scope

- Property-level access (only chain-level for now)
- Pagination of the org list (admin use only, small N)
- Notifications when context switches
- Persisting last-used tenant preference
