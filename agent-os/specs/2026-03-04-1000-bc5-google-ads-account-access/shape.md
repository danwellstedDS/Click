# BC5 — Google Ads Account & Access — Shaping Notes

## Problem

Campaigns need to know which Google Ads accounts a tenant is allowed to use. Without an explicit provider layer, there is no way to:
- Connect a Google MCC (manager) account to a tenant
- Discover the client accounts under that MCC
- Restrict which discovered accounts a tenant can actually use for campaigns

## Approach

Introduce a `googleadsmanagement` bounded context with three core concepts:

1. **GoogleConnection** — one-to-one with a tenant; holds the MCC customer ID and credential path used to call the Google Ads API
2. **AccountGraphState** — a snapshot of accounts discovered under the MCC; refreshed on each discovery run; not an aggregate, just an internal entity
3. **AccountBinding** — explicit record linking a specific Google Ads customer ID to a tenant for campaign use; status tracks ACTIVE / BROKEN / STALE / REMOVED lifecycle

## Key Decisions

- **One connection per tenant** (unique constraint on `tenant_id` in `google_connections`)
- **Service account credentials** stored as a file path; the infra client reads the JSON at call time
- **Scheduled discovery** runs every 60s (check), but only triggers a full API call if the last discovery was >1 hour ago — matches the ScheduleService pattern
- **Stale detection** on each discovery: bindings for customer IDs no longer in the discovered set are flagged STALE; they recover automatically on the next successful discovery if the account reappears
- **No OAuth flow** in BC5 — the service account covers everything via domain-wide delegation
- **`AccountGraphState` is not a full aggregate** — it has no lifecycle events; it's simply deleted and recreated on each discovery run

## Out of Scope (BC5)

- Campaign creation (uses AccountBinding in a later BC)
- Google Ads reporting / metrics pull (separate ingestion context)
- MCC hierarchy deeper than 1 level
