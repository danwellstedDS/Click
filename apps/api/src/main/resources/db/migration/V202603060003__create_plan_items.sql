CREATE TABLE plan_items (
    id UUID PRIMARY KEY,
    revision_id UUID NOT NULL REFERENCES plan_revisions(id),
    tenant_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    action_type VARCHAR(30) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    resource_id VARCHAR(255),
    payload TEXT NOT NULL,
    apply_order INT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMPTZ,
    next_attempt_after TIMESTAMPTZ,
    failure_class VARCHAR(20),
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_item_status CHECK (status IN ('DRAFT','PUBLISHED','QUEUED','IN_PROGRESS','SUCCEEDED','FAILED','BLOCKED','CANCELLED')),
    CONSTRAINT chk_item_action_type CHECK (action_type IN ('CREATE_CAMPAIGN','UPDATE_CAMPAIGN','CREATE_AD_GROUP','UPDATE_AD_GROUP','CREATE_AD','UPDATE_AD','CREATE_KEYWORD','UPDATE_KEYWORD')),
    CONSTRAINT chk_item_resource_type CHECK (resource_type IN ('CAMPAIGN','AD_GROUP','AD','KEYWORD'))
);

CREATE INDEX idx_plan_items_revision_id ON plan_items(revision_id);
CREATE INDEX idx_plan_items_tenant_status ON plan_items(tenant_id, status);
CREATE INDEX idx_plan_items_queued_order ON plan_items(apply_order ASC)
    WHERE status = 'QUEUED';
