CREATE TABLE tenant_memberships (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    tenant_id  UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    role       TEXT NOT NULL CHECK (role IN ('ADMIN','VIEWER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, tenant_id)
);
CREATE INDEX idx_memberships_user_id   ON tenant_memberships (user_id);
CREATE INDEX idx_memberships_tenant_id ON tenant_memberships (tenant_id);
