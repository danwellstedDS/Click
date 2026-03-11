CREATE TABLE canonical_batches (
    id UUID PRIMARY KEY,
    source_snapshot_id UUID NOT NULL,
    integration_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    account_id VARCHAR(20) NOT NULL,
    mapping_version VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    fact_count INT NOT NULL DEFAULT 0,
    quarantined_count INT NOT NULL DEFAULT 0,
    checksum VARCHAR(64),
    produced_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_batch_status CHECK (status IN ('PROCESSING','PRODUCED','FAILED','REBUILT')),
    CONSTRAINT uq_batch_source_version UNIQUE (source_snapshot_id, mapping_version)
);
