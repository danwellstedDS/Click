# BC7 Mock Data Seed — References

## Seed Files Studied

### `V202602200005__seed_dev_user.sql`
- Pattern: plain `INSERT INTO` with fixed UUIDs and literal values.
- Header comment: identifies dev-only purpose and credentials.
- No `ON CONFLICT` — clean-schema assumption.

### `V202603030002__seed_integration_instances.sql`
- Demonstrates multi-row seeding for the same table.
- Integration ID `10000000-0000-0000-0000-000000000001` = Google Ads (used as anchor).
- Uses `NOW()` for timestamp columns only.

### `V202603040004__seed_google_connections.sql`
- Confirms customer ID `506-204-8043` for the demo account.
- Confirms tenant ID `00000000-0000-0000-0000-000000000001`.
- Pattern: separate INSERT per table with descriptive comments.

## Table DDLs Used

### `V202603050001__create_sync_jobs.sql`
- Columns: `id, integration_id, tenant_id, account_id, report_type, date_from, date_to, trigger_type, idempotency_key, status, attempts, max_attempts, triggered_by, created_at, updated_at`
- Status constraint: `PENDING | RUNNING | SUCCEEDED | FAILED | STUCK`
- Trigger type constraint: `DAILY | MANUAL | BACKFILL | FORCE_RUN`

### `V202603050002__create_raw_snapshots.sql`
- `raw_snapshots` columns: `id, sync_job_id, integration_id, tenant_id, account_id, report_type, date_from, date_to, row_count, checksum, created_at`
- `raw_campaign_rows` columns: `id, snapshot_id, integration_id, account_id, campaign_id, campaign_name, report_date, clicks, impressions, cost_micros, conversions, ingested_at`
- Natural key unique constraint: `(integration_id, account_id, campaign_id, report_date)`

### `V202603050003__create_sync_incidents.sql`
- Columns: `id, idempotency_key, tenant_id, failure_class, status, consecutive_failures, first_failed_at, last_failed_at, created_at, updated_at`
- Status constraint: `OPEN | AUTO_CLOSED | REOPENED | ESCALATED`
- Failure class constraint: `TRANSIENT | PERMANENT`
- Unique index on `idempotency_key` where `status IN ('OPEN','REOPENED','ESCALATED')`
