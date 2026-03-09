# BC7 — Mock Data Seed for Normalisation Testing

**Date**: 2026-03-09
**Branch**: feature/bc7-mock-data-seed
**Status**: COMPLETE

## Summary

Adds a single deterministic Flyway seed migration (`V202603090002`) that populates BC7 tables with 30 days of realistic Google Ads data for the demo tenant. Enables BC8 development and testing without a live Google dependency.

## What was added

| File | Change |
|------|--------|
| `apps/api/src/main/resources/db/migration/V202603090002__seed_bc7_mock_data.sql` | New — full BC7 seed |
| `docs/bd.md` | BC7 locked decisions: dev seed data note |

## Seed contents

- **4 sync jobs** (weekly, DAILY trigger, SUCCEEDED)
- **4 raw snapshots** (1 per job)
- **90 raw campaign rows** (3 campaigns × 30 days, 2026-02-06 to 2026-03-07)
- **2 sync incidents** (ESCALATED/TRANSIENT + OPEN/PERMANENT)

## Anchor values

| Value | Source |
|-------|--------|
| Tenant ID | `00000000-0000-0000-0000-000000000001` |
| Integration ID | `10000000-0000-0000-0000-000000000001` |
| Customer ID | `506-204-8043` |

## Verification

```sql
SELECT status, COUNT(*) FROM sync_jobs WHERE id::text LIKE '50000000-0000-0000-0001-%' GROUP BY status;
-- Expected: SUCCEEDED | 4

SELECT COUNT(*) FROM raw_campaign_rows WHERE integration_id = '10000000-0000-0000-0000-000000000001';
-- Expected: 90

SELECT MIN(report_date), MAX(report_date) FROM raw_campaign_rows;
-- Expected: 2026-02-06 | 2026-03-07

SELECT status, failure_class FROM sync_incidents WHERE id::text LIKE '50000000-0000-0000-0003-%';
-- Expected: ESCALATED/TRANSIENT and OPEN/PERMANENT
```
