CREATE TABLE mapping_runs (
    id UUID PRIMARY KEY,
    canonical_batch_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    rule_set_version VARCHAR(20) NOT NULL DEFAULT 'v1',
    override_set_version VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    mapped_count INT NOT NULL DEFAULT 0,
    low_confidence_count INT NOT NULL DEFAULT 0,
    unresolved_count INT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_run_status CHECK (status IN ('RUNNING','PRODUCED','FAILED')),
    CONSTRAINT uq_run_idempotency UNIQUE (canonical_batch_id, rule_set_version, override_set_version)
);
CREATE INDEX idx_mapping_run_batch ON mapping_runs(canonical_batch_id);
CREATE INDEX idx_mapping_run_tenant ON mapping_runs(tenant_id);
