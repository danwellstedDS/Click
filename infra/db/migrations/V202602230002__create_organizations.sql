CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('CHAIN','AGENCY','DERBYSOFT')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_organizations_type ON organizations (type);

ALTER TABLE chains ADD COLUMN primary_org_id UUID REFERENCES organizations(id) ON DELETE SET NULL;
CREATE INDEX idx_chains_primary_org_id ON chains (primary_org_id);
