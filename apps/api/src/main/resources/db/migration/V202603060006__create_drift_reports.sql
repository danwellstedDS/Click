CREATE TABLE drift_reports (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES campaign_plans(id),
    revision_id UUID NOT NULL REFERENCES plan_revisions(id),
    tenant_id UUID NOT NULL,
    severity VARCHAR(10) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    field VARCHAR(255) NOT NULL,
    intended_value TEXT,
    provider_value TEXT,
    detected_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_drift_severity CHECK (severity IN ('HIGH','MEDIUM','LOW'))
);

CREATE INDEX idx_drift_reports_plan_id ON drift_reports(plan_id);
CREATE INDEX idx_drift_reports_revision_id ON drift_reports(revision_id);
