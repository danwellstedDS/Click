-- Replace chain/hotel model with property_group/property model.
-- Dev environment: tables are dropped and recreated.

-- ── Drop old tables (cascade to handle all FK dependencies) ─────────────────
DROP TABLE IF EXISTS scope_access_grants CASCADE;
DROP TABLE IF EXISTS contract_coverages CASCADE;
DROP TABLE IF EXISTS access_scopes CASCADE;
DROP TABLE IF EXISTS portfolio_hotels CASCADE;
DROP TABLE IF EXISTS portfolios CASCADE;
DROP TABLE IF EXISTS hotels CASCADE;
DROP TABLE IF EXISTS chains CASCADE;

-- ── property_groups (self-referential, up to 5 levels deep) ─────────────────
CREATE TABLE property_groups (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id      UUID REFERENCES property_groups(id) ON DELETE CASCADE,
    name           TEXT NOT NULL,
    timezone       TEXT,
    currency       TEXT,
    primary_org_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_property_groups_parent_id      ON property_groups (parent_id);
CREATE INDEX idx_property_groups_primary_org_id ON property_groups (primary_org_id);

-- ── properties (leaf nodes, belong to a PropertyGroup) ──────────────────────
CREATE TABLE properties (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_group_id     UUID NOT NULL REFERENCES property_groups(id) ON DELETE CASCADE,
    name                  TEXT NOT NULL,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    external_property_ref TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_properties_property_group_id ON properties (property_group_id);

-- ── portfolios (FK renamed chain_id → property_group_id) ────────────────────
CREATE TABLE portfolios (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_group_id     UUID NOT NULL REFERENCES property_groups(id) ON DELETE CASCADE,
    name                  TEXT NOT NULL,
    owner_organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (property_group_id, name)
);
CREATE INDEX idx_portfolios_property_group_id     ON portfolios (property_group_id);
CREATE INDEX idx_portfolios_owner_organization_id ON portfolios (owner_organization_id);

-- ── portfolio_properties (renamed from portfolio_hotels) ────────────────────
CREATE TABLE portfolio_properties (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    property_id  UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    UNIQUE (portfolio_id, property_id)
);
CREATE INDEX idx_portfolio_properties_property_id ON portfolio_properties (property_id);

-- ── access_scopes (CHAIN→PROPERTY_GROUP, HOTEL→PROPERTY) ────────────────────
CREATE TABLE access_scopes (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type              TEXT NOT NULL CHECK (type IN ('PROPERTY_GROUP','PROPERTY','PORTFOLIO')),
    property_group_id UUID NOT NULL REFERENCES property_groups(id) ON DELETE CASCADE,
    property_id       UUID REFERENCES properties(id) ON DELETE CASCADE,
    portfolio_id      UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_access_scopes_property_group_id_type ON access_scopes (property_group_id, type);
CREATE UNIQUE INDEX uq_access_scope_property_group
    ON access_scopes (property_group_id) WHERE type = 'PROPERTY_GROUP';
CREATE UNIQUE INDEX uq_access_scope_property
    ON access_scopes (property_group_id, property_id) WHERE type = 'PROPERTY';
CREATE UNIQUE INDEX uq_access_scope_portfolio
    ON access_scopes (property_group_id, portfolio_id) WHERE type = 'PORTFOLIO';

-- ── scope_access_grants (structure unchanged, recreated) ────────────────────
CREATE TABLE scope_access_grants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    scope_id        UUID NOT NULL REFERENCES access_scopes(id) ON DELETE CASCADE,
    role            TEXT NOT NULL CHECK (role IN ('VIEWER','ANALYST','MANAGER','ADMIN','SUPPORT')),
    valid_from      TIMESTAMPTZ,
    valid_to        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organization_id, scope_id, role)
);
CREATE INDEX idx_scope_access_grants_scope_id        ON scope_access_grants (scope_id);
CREATE INDEX idx_scope_access_grants_organization_id ON scope_access_grants (organization_id);

-- ── Seed data (replaces V202602230009 seed) ──────────────────────────────────
-- Credentials: admin@example.com / password

-- Remove old seed data
DELETE FROM users WHERE id = '00000000-0000-0000-0000-000000000002';
DELETE FROM organizations WHERE id = '00000000-0000-0000-0000-000000000010';

-- 1. Root PropertyGroup (Demo Property Group)
INSERT INTO property_groups (id, name, timezone, currency)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Property Group', 'UTC', 'USD');

-- 2. Organization (type CHAIN, "Demo Organization")
INSERT INTO organizations (id, name, type)
VALUES ('00000000-0000-0000-0000-000000000010', 'Demo Organization', 'CHAIN');

-- 3. Link property group to primary organization
UPDATE property_groups
SET primary_org_id = '00000000-0000-0000-0000-000000000010'
WHERE id = '00000000-0000-0000-0000-000000000001';

-- 4. Property (Demo Property, linked to property group)
INSERT INTO properties (id, property_group_id, name, is_active)
VALUES ('00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000001', 'Demo Property', TRUE);

-- 5. User (admin@example.com)
INSERT INTO users (id, email, password_hash, name, is_active)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'admin@example.com',
    '$2b$12$Zc/fFtauvGeNdGd6sxK04OxT1dCOj8Z2EGCw9v25RQmVJUychXMRy',
    'Demo Admin',
    TRUE
);

-- 6. OrgMembership (user → org, isOrgAdmin = true)
INSERT INTO org_memberships (user_id, organization_id, is_org_admin)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000010',
    TRUE
);

-- 7. AccessScope (type PROPERTY_GROUP, property_group_id = demo property group)
INSERT INTO access_scopes (id, type, property_group_id)
VALUES ('00000000-0000-0000-0000-000000000030', 'PROPERTY_GROUP', '00000000-0000-0000-0000-000000000001');

-- 8. ScopeAccessGrant (org → scope, role = ADMIN)
INSERT INTO scope_access_grants (organization_id, scope_id, role)
VALUES (
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000030',
    'ADMIN'
);
