# BC7 Mock Data Seed — Relevant Standards

## Database / Migrations

- Flyway versioned migrations only — no repeatable (`R__`) migrations for seed data.
- Migration version format: `V{YYYYMMDD}{NNNN}__description.sql` (4-digit sequence).
- Seed migrations must include a header comment flagging them as dev-only.
- Fixed UUIDs for seed rows to ensure determinism across environments.
- Use `NOW()` only for audit timestamps (`created_at`, `updated_at`, `ingested_at`); never for business dates in seed data.
- Foreign key order: insert parent rows before child rows (sync_jobs → raw_snapshots → raw_campaign_rows).
- No `ON CONFLICT` clauses in seed migrations — seeds run once on a clean schema.

## Backend Standards

- No Java code changes for seed-only work.
- Seed migrations are excluded from production deployment via environment gating (dev/CI only).
- UUID namespaces must not collide with existing seed ranges (`00000000-*`, `10000000-*`, `20000000-*`, `30000000-*`).
- `raw_campaign_rows` natural key: `(integration_id, account_id, campaign_id, report_date)` — one row per campaign per day.
- `sync_incidents` idempotency_key unique constraint applies only to OPEN/REOPENED/ESCALATED status — seeds must not create duplicate open incidents for the same key.
