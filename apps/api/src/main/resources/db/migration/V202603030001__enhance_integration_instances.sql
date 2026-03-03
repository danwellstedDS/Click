-- 1. Add new columns (nullable first for safe migration)
ALTER TABLE integration_instances
    ADD COLUMN connection_key         VARCHAR(100),
    ADD COLUMN cadence_type           VARCHAR(20),
    ADD COLUMN cron_expression        VARCHAR(100),
    ADD COLUMN interval_minutes       INTEGER,
    ADD COLUMN last_sync_at           TIMESTAMPTZ,
    ADD COLUMN last_sync_status       VARCHAR(20),
    ADD COLUMN last_success_at        TIMESTAMPTZ,
    ADD COLUMN last_error_code        VARCHAR(50),
    ADD COLUMN last_error_message     TEXT,
    ADD COLUMN consecutive_failures   INTEGER,
    ADD COLUMN status_reason          TEXT,
    ADD COLUMN credential_attached_at TIMESTAMPTZ,
    ADD COLUMN updated_by             UUID;

-- 2. Migrate data from old columns
UPDATE integration_instances SET
    connection_key       = 'default',
    cadence_type         = 'CRON',
    cron_expression      = sync_schedule_cron,
    last_sync_status     = 'NEVER',
    consecutive_failures = 0;

-- 3. Apply NOT NULL where needed
ALTER TABLE integration_instances
    ALTER COLUMN connection_key       SET NOT NULL,
    ALTER COLUMN cadence_type         SET NOT NULL,
    ALTER COLUMN last_sync_status     SET NOT NULL,
    ALTER COLUMN consecutive_failures SET NOT NULL;

-- 4. Rename timezone column for clarity (avoids confusion with hotel/account timezone)
ALTER TABLE integration_instances
    RENAME COLUMN sync_schedule_timezone TO schedule_timezone;

-- 5. Drop old cron column
ALTER TABLE integration_instances
    DROP COLUMN sync_schedule_cron;

-- 6. Drop old unique constraint, add new one
ALTER TABLE integration_instances
    DROP CONSTRAINT uq_tenant_channel;
ALTER TABLE integration_instances
    ADD CONSTRAINT uq_tenant_channel_connection
        UNIQUE (tenant_id, channel, connection_key);

-- 7. Check constraints
ALTER TABLE integration_instances
    ADD CONSTRAINT chk_active_requires_credential
        CHECK (status != 'Active' OR credential_ref_id IS NOT NULL),
    ADD CONSTRAINT chk_cron_requires_expression
        CHECK (cadence_type != 'CRON' OR cron_expression IS NOT NULL),
    ADD CONSTRAINT chk_interval_requires_minutes
        CHECK (cadence_type != 'INTERVAL' OR (interval_minutes IS NOT NULL AND interval_minutes >= 5)),
    ADD CONSTRAINT chk_manual_has_no_schedule
        CHECK (cadence_type != 'MANUAL' OR (cron_expression IS NULL AND interval_minutes IS NULL));

-- 8. New index for scheduler query
CREATE INDEX idx_ii_status_cadence ON integration_instances (status, cadence_type);
