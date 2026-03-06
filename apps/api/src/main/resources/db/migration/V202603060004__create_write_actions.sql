CREATE TABLE write_actions (
    id UUID PRIMARY KEY,
    revision_id UUID NOT NULL REFERENCES plan_revisions(id),
    item_id UUID NOT NULL REFERENCES plan_items(id),
    tenant_id UUID NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    last_attempt_at TIMESTAMPTZ,
    lease_expires_at TIMESTAMPTZ,
    next_attempt_after TIMESTAMPTZ,
    failure_class VARCHAR(20),
    failure_reason TEXT,
    triggered_by VARCHAR(255),
    trigger_type VARCHAR(20) NOT NULL,
    trigger_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_write_action_status CHECK (status IN ('PENDING','RUNNING','SUCCEEDED','FAILED','CANCELLED')),
    CONSTRAINT chk_write_action_trigger_type CHECK (trigger_type IN ('SCHEDULED','MANUAL','FORCE_RUN','RETRY'))
);

CREATE UNIQUE INDEX uq_write_action_idempotency_active ON write_actions(idempotency_key)
    WHERE status IN ('PENDING','RUNNING');

CREATE INDEX idx_write_actions_revision_id ON write_actions(revision_id);
CREATE INDEX idx_write_actions_item_id ON write_actions(item_id);
CREATE INDEX idx_write_actions_tenant_status ON write_actions(tenant_id, status);
CREATE INDEX idx_write_actions_lease ON write_actions(status, lease_expires_at);
CREATE INDEX idx_write_actions_next_attempt ON write_actions(status, next_attempt_after);
