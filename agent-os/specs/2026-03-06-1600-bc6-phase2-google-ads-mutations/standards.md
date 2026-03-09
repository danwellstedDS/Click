# Standards — BC6 Phase 2

## API Client Pattern

- Mirror `GoogleAdsReportingClient` for `buildClient()`, `handleGoogleAdsException()`, `handleStatusRuntimeException()`.
- Use `GoogleAdsConfig` (shared config bean) for `credentialsPath` and `developerToken`.
- `setLoginCustomerId()` takes the manager ID (long, stripped of dashes).
- Service clients are opened with try-with-resources.
- All proto enums resolved via `valueOf()` on the generated enum type.

## Exception Hierarchy

- Auth errors (`OAUTH_TOKEN`, `NOT_AUTHORIZED`, `CUSTOMER_NOT_FOUND`) → `MutationAuthException` → `FailureClass.PERMANENT`
- API errors (non-auth `GoogleAdsException`) → `MutationApiException(PERMANENT)`
- gRPC transient (`DEADLINE_EXCEEDED`, `UNAVAILABLE`, `RESOURCE_EXHAUSTED`) → `MutationApiException(TRANSIENT)`
- gRPC other → `MutationApiException(PERMANENT)`

## Domain Invariants

- `CampaignPlan.targetCustomerId` is set at creation; immutable.
- `WriteAction.targetCustomerId` is snapshot-set at apply time from `CampaignPlan`.
- Both DB columns nullable for backward compatibility with existing rows.

## DB Migration

- Flyway migration `V202603060007__add_customer_id_to_plans_and_actions.sql`.
- Nullable columns; no data backfill required.
