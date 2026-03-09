# BC7 Mock Data Seed — Shaping Notes

## Problem

BC8 (Normalisation) consumes BC7 raw campaign data. No mock BC7 data existed, making it impossible to develop or test BC8 against realistic inputs without a live Google Ads dependency.

## Approach

Single deterministic Flyway seed migration. Fixed UUIDs and dates ensure stable, reproducible behaviour across all dev environments and CI runs. No Java changes needed — data layer only.

## Key Decisions

### Stable data (no `NOW()` for business dates)
- All `report_date` values are fixed calendar dates (2026-02-06 to 2026-03-07).
- `created_at`/`updated_at`/`ingested_at` timestamps use `NOW()` (harmless for seed data).

### UUID namespace
- `50000000-0000-0000-0001-*` → sync_jobs
- `50000000-0000-0000-0002-*` → raw_snapshots
- `50000000-0000-0000-0003-*` → sync_incidents
- `50000000-0000-0000-0004-*` → raw_campaign_rows (sequential per campaign per day)

### Campaign portfolio
Three campaigns representing a typical hotel account:
1. `Hotel Grand | Brand` — high CTR, strong conversions, lower impressions
2. `Hotel Grand | Generic Search` — very high impressions, lower CTR, moderate cost
3. `Hotel Grand | Competitor` — moderate impressions, high CPC, few conversions

### Edge cases included
- 3 zero-impression days (one per campaign — simulates campaign pause)
- 2 high-cost spike days (simulates month-end budget flush)
- Fractional `conversions` values (e.g., `3.50`) exercising `NUMERIC(12,2)` column

### Incident scenarios
- ESCALATED/TRANSIENT: repeated transient failure escalated after 3 consecutive failures
- OPEN/PERMANENT: fresh auth failure, non-retryable

## Scope

- CAMPAIGN_PERFORMANCE report type only (matches BC7 MVP scope)
- Single tenant, single integration, single customer account
- No multi-account or multi-tenant scenarios in this seed (deferred to future fixture versions)
