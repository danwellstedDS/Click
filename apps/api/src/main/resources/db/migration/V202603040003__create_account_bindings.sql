CREATE TABLE account_bindings (
    id UUID PRIMARY KEY,
    connection_id UUID NOT NULL REFERENCES google_connections(id),
    tenant_id UUID NOT NULL,
    customer_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    binding_type VARCHAR(20) NOT NULL DEFAULT 'OWNED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_binding_status CHECK (status IN ('ACTIVE', 'BROKEN', 'STALE', 'REMOVED')),
    CONSTRAINT chk_binding_type CHECK (binding_type IN ('OWNED', 'ACCESSIBLE'))
);
CREATE UNIQUE INDEX uq_binding_active ON account_bindings(connection_id, customer_id)
    WHERE status != 'REMOVED';
CREATE INDEX idx_binding_tenant_id ON account_bindings(tenant_id);
CREATE INDEX idx_binding_connection_id ON account_bindings(connection_id);
