DROP TABLE tenant_memberships;
ALTER TABLE tenants RENAME TO chains;
ALTER INDEX idx_tenants_created_at RENAME TO idx_chains_created_at;
ALTER TABLE chains ADD COLUMN timezone TEXT;
ALTER TABLE chains ADD COLUMN currency TEXT;
ALTER TABLE users ADD COLUMN name TEXT;
ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
