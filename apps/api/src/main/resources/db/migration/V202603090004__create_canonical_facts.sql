CREATE TABLE canonical_facts (
    id UUID PRIMARY KEY,
    canonical_batch_id UUID NOT NULL REFERENCES canonical_batches(id),
    source_snapshot_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    channel VARCHAR(30) NOT NULL,
    integration_id UUID NOT NULL,
    customer_account_id VARCHAR(20) NOT NULL,
    campaign_id VARCHAR(30) NOT NULL,
    campaign_name VARCHAR(255),
    report_date DATE NOT NULL,
    impressions BIGINT NOT NULL DEFAULT 0,
    clicks BIGINT NOT NULL DEFAULT 0,
    cost_micros BIGINT NOT NULL DEFAULT 0,
    conversions NUMERIC(12,2) NOT NULL DEFAULT 0,
    mapping_version VARCHAR(20) NOT NULL,
    reconciliation_key VARCHAR(64) NOT NULL,
    quality_flags TEXT[] NOT NULL DEFAULT '{}',
    quarantined BOOLEAN NOT NULL DEFAULT false,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_canonical_fact_batch_key UNIQUE (canonical_batch_id, reconciliation_key)
);
CREATE INDEX idx_canonical_fact_batch ON canonical_facts(canonical_batch_id);
CREATE INDEX idx_canonical_fact_snapshot ON canonical_facts(source_snapshot_id);
CREATE INDEX idx_canonical_fact_account_date ON canonical_facts(customer_account_id, report_date);
CREATE INDEX idx_canonical_fact_quarantined ON canonical_facts(quarantined) WHERE quarantined = true;
