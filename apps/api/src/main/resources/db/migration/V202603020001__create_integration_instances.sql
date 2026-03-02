CREATE TABLE integration_instances (
    id                     UUID         NOT NULL PRIMARY KEY,
    tenant_id              UUID         NOT NULL,
    channel                VARCHAR(50)  NOT NULL,
    status                 VARCHAR(20)  NOT NULL,
    credential_ref_id      UUID,
    sync_schedule_cron     VARCHAR(100) NOT NULL,
    sync_schedule_timezone VARCHAR(100) NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tenant_channel UNIQUE (tenant_id, channel)
);

CREATE INDEX idx_ii_tenant_id ON integration_instances (tenant_id);
CREATE INDEX idx_ii_status    ON integration_instances (status);
