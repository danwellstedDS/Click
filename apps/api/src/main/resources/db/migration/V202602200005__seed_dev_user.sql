-- Dev seed data: test user + tenant + membership
-- Credentials: admin@example.com / password
-- Remove or gate this migration before deploying to production.

INSERT INTO tenants (id, name)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Tenant');

INSERT INTO users (id, email, password_hash)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'admin@example.com',
    '$2b$12$Zc/fFtauvGeNdGd6sxK04OxT1dCOj8Z2EGCw9v25RQmVJUychXMRy'
);

INSERT INTO tenant_memberships (user_id, tenant_id, role)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'ADMIN'
);
