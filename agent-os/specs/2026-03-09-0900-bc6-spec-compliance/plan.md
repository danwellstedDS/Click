# BC6 — Spec Compliance Gaps (11 Fixes)

**Date**: 2026-03-09
**Branch**: feature/bc6-phase2-real-google-ads-mutations
**Status**: COMPLETE

## Summary

Closed 11 gaps where the BC6 implementation diverged from the spec in `docs/bd.md`. All changes are within the `campaignexecution` module plus single shared-kernel (`EventEnvelope`) and BC5 (`AccessFailureObservedHandler`) changes.

## Gaps closed

| # | Gap | Primary files changed |
|---|-----|-----------------------|
| 1 | BC2 governance gate missing at publish + execution time | `PublishValidationService`, `WriteActionExecutor`, `TenantGovernancePort` |
| 2 | Binding validation picks first binding; should fail fast if no binding for tenant | `PlanApplyService` |
| 3 | `AccessFailureObserved` cross-BC event to BC5 missing on auth errors | `WriteActionExecutor`, `AccessFailureObservedHandler` (new) |
| 4 | Incident identity was `idempotencyKey` string; spec requires `(revisionId, itemId, failureClass)` | `ExecutionIncident`, `ExecutionIncidentRepository`, entity, mapper, migration |
| 5 | `recordFailure()` reopened `AUTO_CLOSED` without checking 24h recurrence window | `ExecutionIncidentLifecycleService`, `ExecutionIncident.isRecurrenceWindowExpired()` |
| 6 | `APPLY` trigger not counted against manual rate limit | `ManualExecutionRateLimitService`, `PlanApplyService`, `TriggerType` |
| 7 | Rate-limit exception lacked `retryAfter`; no `Retry-After` header; `reason` not validated | `RateLimitExceededException` (new), `CampaignPlanController`, `ApplyRevisionRequest` |
| 8 | `PlanItemResponse` and `ExecutionIncidentResponse` missing `nextAction` and `actionability` | Both DTOs, `PlanItemController`, `ExecutionIncidentController` |
| 9 | `DriftDetectionService.detect()` was a stub | `DriftDetectionService`, `SnapshotQueryPort` (new port), `DriftItem` |
| 10 | `RevisionCompletionChecker` never emitted `ExecutionSummaryUpdated` | `RevisionCompletionChecker` |
| 11 | `EventEnvelope` missing 5 required fields | `EventEnvelope`, `ModuleRegistry` (stub bean for `SnapshotQueryPort`) |

## Verification

All 74 `campaignexecution` tests pass:
```
./gradlew :apps:api:test --tests "*.campaignexecution.*"
```
