CREATE TABLE scope_access_grants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    scope_id UUID NOT NULL REFERENCES access_scopes(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('VIEWER','ANALYST','MANAGER','ADMIN','SUPPORT')),
    valid_from TIMESTAMPTZ,
    valid_to TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organization_id, scope_id, role)
);
CREATE INDEX idx_scope_access_grants_scope_id ON scope_access_grants (scope_id);
CREATE INDEX idx_scope_access_grants_organization_id ON scope_access_grants (organization_id);
