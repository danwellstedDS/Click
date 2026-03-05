CREATE TABLE raw_snapshots (
    id UUID PRIMARY KEY,
    sync_job_id UUID NOT NULL REFERENCES sync_jobs(id),
    integration_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    account_id VARCHAR(20) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    row_count INT NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_snapshot_job_id ON raw_snapshots(sync_job_id);
CREATE INDEX idx_snapshot_integration ON raw_snapshots(integration_id, tenant_id);

CREATE TABLE raw_campaign_rows (
    id UUID PRIMARY KEY,
    snapshot_id UUID NOT NULL REFERENCES raw_snapshots(id) ON DELETE CASCADE,
    integration_id UUID NOT NULL,
    account_id VARCHAR(20) NOT NULL,
    campaign_id VARCHAR(30) NOT NULL,
    campaign_name VARCHAR(255),
    report_date DATE NOT NULL,
    clicks BIGINT NOT NULL DEFAULT 0,
    impressions BIGINT NOT NULL DEFAULT 0,
    cost_micros BIGINT NOT NULL DEFAULT 0,
    conversions NUMERIC(12,2) NOT NULL DEFAULT 0,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_campaign_row_natural_key UNIQUE (integration_id, account_id, campaign_id, report_date)
);

CREATE INDEX idx_campaign_row_snapshot ON raw_campaign_rows(snapshot_id);
CREATE INDEX idx_campaign_row_account_date ON raw_campaign_rows(account_id, report_date);
