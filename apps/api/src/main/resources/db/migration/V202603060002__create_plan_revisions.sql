CREATE TABLE plan_revisions (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES campaign_plans(id),
    tenant_id UUID NOT NULL,
    revision_number INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_by VARCHAR(255),
    published_at TIMESTAMPTZ,
    cancelled_by VARCHAR(255),
    cancel_reason TEXT,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_revision_status CHECK (status IN ('DRAFT','PUBLISHED','APPLYING','APPLIED','CANCELLED','FAILED')),
    CONSTRAINT uq_plan_revision_number UNIQUE (plan_id, revision_number)
);

CREATE INDEX idx_plan_revisions_tenant_status ON plan_revisions(tenant_id, status);
CREATE INDEX idx_plan_revisions_plan_id ON plan_revisions(plan_id);
