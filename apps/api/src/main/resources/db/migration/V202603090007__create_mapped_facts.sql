CREATE TABLE mapped_facts (
    id UUID PRIMARY KEY,
    mapping_run_id UUID NOT NULL REFERENCES mapping_runs(id),
    canonical_fact_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    resolved_org_node_id UUID,
    resolved_scope_type VARCHAR(30),
    confidence_band VARCHAR(20) NOT NULL,
    confidence_score NUMERIC(4,3) NOT NULL DEFAULT 0,
    resolution_reason_code VARCHAR(50) NOT NULL,
    rule_set_version VARCHAR(20) NOT NULL,
    override_applied BOOLEAN NOT NULL DEFAULT false,
    mapped_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_confidence_band CHECK (confidence_band IN ('HIGH','MEDIUM','LOW','UNRESOLVED')),
    CONSTRAINT uq_mapped_fact_run_canonical UNIQUE (mapping_run_id, canonical_fact_id)
);
CREATE INDEX idx_mapped_fact_run ON mapped_facts(mapping_run_id);
CREATE INDEX idx_mapped_fact_canonical ON mapped_facts(canonical_fact_id);
CREATE INDEX idx_mapped_fact_low_conf ON mapped_facts(confidence_band)
    WHERE confidence_band IN ('LOW','UNRESOLVED');
