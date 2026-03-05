CREATE TABLE google_connections (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL UNIQUE,
    manager_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    credential_path VARCHAR(500) NOT NULL,
    last_discovered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_connection_status CHECK (status IN ('ACTIVE', 'BROKEN'))
);
CREATE INDEX idx_google_connections_status ON google_connections(status);
