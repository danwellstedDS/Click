CREATE TABLE sync_incidents (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    failure_class VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    consecutive_failures INT NOT NULL DEFAULT 1,
    first_failed_at TIMESTAMPTZ NOT NULL,
    last_failed_at TIMESTAMPTZ NOT NULL,
    acknowledged_by VARCHAR(255),
    ack_reason TEXT,
    acknowledged_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_incident_status CHECK (status IN ('OPEN','AUTO_CLOSED','REOPENED','ESCALATED')),
    CONSTRAINT chk_failure_class CHECK (failure_class IN ('TRANSIENT','PERMANENT'))
);

CREATE UNIQUE INDEX uq_incident_key_open ON sync_incidents(idempotency_key)
    WHERE status IN ('OPEN','REOPENED','ESCALATED');

CREATE INDEX idx_incident_tenant_status ON sync_incidents(tenant_id, status);
