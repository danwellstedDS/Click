CREATE TABLE mapping_overrides (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    scope_type VARCHAR(30) NOT NULL,
    customer_account_id VARCHAR(20) NOT NULL,
    campaign_id VARCHAR(30),
    target_org_node_id UUID NOT NULL,
    target_scope_type VARCHAR(30) NOT NULL,
    reason TEXT NOT NULL,
    actor VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    removed_at TIMESTAMPTZ,
    removed_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_override_status CHECK (status IN ('ACTIVE','REMOVED')),
    CONSTRAINT chk_override_scope CHECK (scope_type IN ('ACCOUNT','ACCOUNT_CAMPAIGN'))
);
CREATE INDEX idx_override_tenant ON mapping_overrides(tenant_id);
CREATE INDEX idx_override_account ON mapping_overrides(tenant_id, customer_account_id)
    WHERE status = 'ACTIVE';
