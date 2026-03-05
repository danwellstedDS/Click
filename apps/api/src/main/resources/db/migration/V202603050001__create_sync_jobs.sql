CREATE TABLE sync_jobs (
    id UUID PRIMARY KEY,
    integration_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    account_id VARCHAR(20) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    last_attempt_at TIMESTAMPTZ,
    lease_expires_at TIMESTAMPTZ,
    next_attempt_after TIMESTAMPTZ,
    failure_class VARCHAR(20),
    failure_reason TEXT,
    triggered_by VARCHAR(255),
    trigger_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_job_status CHECK (status IN ('PENDING','RUNNING','SUCCEEDED','FAILED','STUCK')),
    CONSTRAINT chk_trigger_type CHECK (trigger_type IN ('DAILY','MANUAL','BACKFILL','FORCE_RUN'))
);

CREATE UNIQUE INDEX uq_job_idempotency_active ON sync_jobs(idempotency_key)
    WHERE status IN ('PENDING','RUNNING');

CREATE INDEX idx_job_tenant_id ON sync_jobs(tenant_id);
CREATE INDEX idx_job_integration_id ON sync_jobs(integration_id);
CREATE INDEX idx_job_status ON sync_jobs(status);
CREATE INDEX idx_job_lease ON sync_jobs(status, lease_expires_at);
CREATE INDEX idx_job_next_attempt ON sync_jobs(status, next_attempt_after);
