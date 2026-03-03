-- 1. Manual sync only — no credential yet (SetupRequired)
INSERT INTO integration_instances (
    id, tenant_id, channel, status,
    connection_key, cadence_type, schedule_timezone,
    last_sync_status, consecutive_failures,
    created_at, updated_at
) VALUES (
    '10000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'GOOGLE_ADS', 'SetupRequired',
    'default', 'MANUAL', 'UTC',
    'NEVER', 0,
    NOW(), NOW()
);

-- 2. Interval sync — active, credential attached, has synced successfully
INSERT INTO integration_instances (
    id, tenant_id, channel, status, credential_ref_id,
    connection_key, cadence_type, interval_minutes, schedule_timezone,
    last_sync_at, last_sync_status, last_success_at, consecutive_failures,
    credential_attached_at, created_at, updated_at
) VALUES (
    '10000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'META_ADS', 'Active',
    '20000000-0000-0000-0000-000000000001',
    'default', 'INTERVAL', 60, 'UTC',
    NOW() - INTERVAL '30 minutes', 'SUCCESS', NOW() - INTERVAL '30 minutes', 0,
    NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NOW()
);

-- 3. Cron sync — broken after failures
INSERT INTO integration_instances (
    id, tenant_id, channel, status, credential_ref_id,
    connection_key, cadence_type, cron_expression, schedule_timezone,
    last_sync_at, last_sync_status, last_success_at,
    last_error_code, last_error_message,
    consecutive_failures, status_reason, credential_attached_at,
    created_at, updated_at
) VALUES (
    '10000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001',
    'GOOGLE_ADS', 'Broken',
    '20000000-0000-0000-0000-000000000002',
    'primary', 'CRON', '0 */6 * * *', 'UTC',
    NOW() - INTERVAL '6 hours', 'FAILED', NOW() - INTERVAL '1 day',
    'AUTH_EXPIRED', 'OAuth token has expired',
    3, 'Sync failing due to expired credentials', NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '2 days', NOW() - INTERVAL '6 hours'
);
