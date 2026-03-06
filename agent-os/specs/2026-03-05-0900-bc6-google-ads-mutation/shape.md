# BC6 Shaping Notes

## Key Decisions

- **Publish gate**: DRAFT → PUBLISHED → APPLYING → APPLIED/FAILED. Prevents accidental apply of
  incomplete revisions.
- **Partial apply**: Each PlanItem executes independently. A revision can complete with mixed
  SUCCEEDED/FAILED items.
- **Idempotency key**: `revisionId:itemId:actionType:targetVersion` — scoped per revision+item to
  allow retries without double-applying.
- **Lease-based execution**: WriteAction acquires a 10-minute lease before calling the Google Ads
  API. Expired leases are recovered by the scheduler.
- **Incident lifecycle**: mirrors BC7 exactly — open on first failure, escalate at 3 consecutive,
  auto-close on success.
- **Rate limit**: 3 manual actions/hour/tenant (FORCE_RUN + RETRY combined).
- **Stub client (MVP)**: `GoogleAdsMutationClient` logs intent and returns success. Real API calls
  deferred to Phase 2.
- **DriftDetectionService**: stub for MVP; shells out for Phase 2.

## Constraints

- No delete operations in MVP.
- `payload` stored as JSON TEXT; validated at publish time only.
- `applyOrder` enforces CAMPAIGN → AD_GROUP → AD → KEYWORD ordering.
- `WriteActionRepositoryImpl` implements dual interfaces: domain port + public query port.
