CREATE TABLE customer_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT NOT NULL CHECK (type IN ('CHAIN_CUSTOMER','AGENCY_CUSTOMER')),
    name TEXT NOT NULL,
    organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_customer_accounts_type ON customer_accounts (type);
CREATE INDEX idx_customer_accounts_organization_id ON customer_accounts (organization_id);

CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_account_id UUID NOT NULL REFERENCES customer_accounts(id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('ACTIVE','PAUSED','CANCELLED')),
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_contracts_customer_account_status ON contracts (customer_account_id, status);

CREATE TABLE contract_coverages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    scope_id UUID NOT NULL REFERENCES access_scopes(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (contract_id, scope_id)
);
CREATE INDEX idx_contract_coverages_scope_id ON contract_coverages (scope_id);
