-- Dev seed data: new product data structure model
-- Credentials: admin@example.com / password
-- Remove or gate this migration before deploying to production.

-- Remove old seed data
DELETE FROM users WHERE id = '00000000-0000-0000-0000-000000000002';
DELETE FROM chains WHERE id = '00000000-0000-0000-0000-000000000001';

-- 1. Chain (Demo Chain)
INSERT INTO chains (id, name, timezone, currency)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Chain', 'UTC', 'USD');

-- 2. Organization (type CHAIN, "Demo Organization")
INSERT INTO organizations (id, name, type)
VALUES ('00000000-0000-0000-0000-000000000010', 'Demo Organization', 'CHAIN');

-- 3. Link chain to primary organization
UPDATE chains
SET primary_org_id = '00000000-0000-0000-0000-000000000010'
WHERE id = '00000000-0000-0000-0000-000000000001';

-- 4. Hotel (Demo Hotel, linked to chain)
INSERT INTO hotels (id, chain_id, name, is_active)
VALUES ('00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000001', 'Demo Hotel', TRUE);

-- 5. User (admin@example.com, same UUID as before for auth compatibility)
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

-- 7. AccessScope (type CHAIN, chain_id = demo chain)
INSERT INTO access_scopes (id, type, chain_id)
VALUES ('00000000-0000-0000-0000-000000000030', 'CHAIN', '00000000-0000-0000-0000-000000000001');

-- 8. ScopeAccessGrant (org → scope, role = ADMIN)
INSERT INTO scope_access_grants (organization_id, scope_id, role)
VALUES (
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000030',
    'ADMIN'
);
