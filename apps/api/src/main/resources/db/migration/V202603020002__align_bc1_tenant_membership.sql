-- Align BC1: rename org_memberships → tenant_memberships,
-- re-point FK from organizations.id → property_groups.id,
-- replace is_org_admin with role column.
--
-- Design decision: PropertyGroup IS the tenant (no separate tenants table).
-- The data migration bridge: org_memberships.organization_id = property_groups.primary_org_id.
-- Confirmed by UserManagementHandler.resolveOrgId() which did the reverse lookup.

-- ── 1. Drop old FK and unique constraint on organization_id ───────────────────
ALTER TABLE org_memberships DROP CONSTRAINT IF EXISTS org_memberships_organization_id_fkey;
ALTER TABLE org_memberships DROP CONSTRAINT IF EXISTS org_memberships_user_id_organization_id_key;
DROP INDEX IF EXISTS idx_org_memberships_organization_id;

-- ── 2. Rename table ───────────────────────────────────────────────────────────
ALTER TABLE org_memberships RENAME TO tenant_memberships;

-- ── 3. Rename column organization_id → tenant_id ─────────────────────────────
ALTER TABLE tenant_memberships RENAME COLUMN organization_id TO tenant_id;

-- ── 4. Data migration: replace org ID with property_group ID ─────────────────
-- org_memberships.organization_id held the primary_org_id of a PropertyGroup,
-- so we look up the matching property_groups.id via primary_org_id.
UPDATE tenant_memberships tm
SET tenant_id = pg.id
FROM property_groups pg
WHERE pg.primary_org_id = tm.tenant_id;

-- ── 5. Drop is_org_admin, add role ────────────────────────────────────────────
ALTER TABLE tenant_memberships DROP COLUMN is_org_admin;
ALTER TABLE tenant_memberships ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'VIEWER';

-- ── 6. Seed admin user: set role to ADMIN for previously-admin memberships ────
-- The dev seed user was the only membership and was isOrgAdmin=true;
-- since is_org_admin is dropped we fix it here for the known seed UUID.
UPDATE tenant_memberships
SET role = 'ADMIN'
WHERE user_id = '00000000-0000-0000-0000-000000000002';

-- ── 7. Add FK constraint to property_groups ───────────────────────────────────
ALTER TABLE tenant_memberships
    ADD CONSTRAINT fk_tm_tenant FOREIGN KEY (tenant_id) REFERENCES property_groups(id) ON DELETE CASCADE;

-- ── 8. Restore unique constraint and index ────────────────────────────────────
ALTER TABLE tenant_memberships
    ADD CONSTRAINT tenant_memberships_user_id_tenant_id_key UNIQUE (user_id, tenant_id);

DROP INDEX IF EXISTS idx_org_memberships_user_id;
CREATE INDEX idx_tenant_memberships_user_id   ON tenant_memberships (user_id);
CREATE INDEX idx_tenant_memberships_tenant_id ON tenant_memberships (tenant_id);
