# BC7 Ingestion (Jobs + Raw Data) — Implementation Plan

## Context

BC7 introduces the `ingestion` bounded context, responsible for:
- Scheduling and orchestrating provider data pulls (Google Ads campaign metrics)
- Enforcing idempotency and single-active-job semantics per idempotency key
- Classifying failures and applying retry/escalation policies
- Persisting raw snapshots + job audit trail
- Publishing `RawSnapshotWritten` for BC8 (Normalisation) and sync outcomes for BC4

Downstream contract: BC7 is the authoritative source of raw data and sync outcomes for the rest of the pipeline.

**Open-question defaults:**
- Event payload contract: standard `(id, tenantId, occurredAt)` + relevant fields, matching existing event record pattern
- Natural key for raw upsert-overwrite: `(integration_id, account_id, campaign_id, report_date)` on `raw_campaign_rows`
- Lease semantics: `lease_expires_at TIMESTAMPTZ` set to `now() + 60min` on job start; RUNNING jobs with expired lease reset to PENDING on next scheduler tick (auto-requeue once, then escalate)
- BC7↔BC4/BC5 auth-failure: publish `SyncFailed(PERMANENT)` + `AuthFailureDetected` on InProcessEventBus
- Retention policy: no automated cleanup in MVP

---

## Task 1 — Spec Documentation

Directory: `agent-os/specs/2026-03-05-0900-bc7-ingestion-jobs-raw-data/`
Files: `plan.md`, `shape.md`, `standards.md`, `references.md`

---

## Task 2 — Domain Layer

Package: `com.derbysoft.click.modules.ingestion.domain`

### Value Objects
- `SyncJobStatus` (enum): PENDING, RUNNING, SUCCEEDED, FAILED, STUCK
- `TriggerType` (enum): DAILY, MANUAL, BACKFILL, FORCE_RUN
- `FailureClass` (enum): TRANSIENT, PERMANENT
- `IncidentStatus` (enum): OPEN, AUTO_CLOSED, REOPENED, ESCALATED
- `DateWindow` (record): from, to — `toKey()` → "from_to", `days()` → count
- `IdempotencyKey` (record): integrationId, accountId, dateWindow, reportType — `toKey()`

### Aggregates
- `SyncJob`: PENDING→RUNNING→SUCCEEDED/FAILED/STUCK; `acquireLease()`, `markSucceeded()`, `markFailed()`, `markStuck()`, `requeueForRetry()`, `canRetry()`
- `RawSnapshot`: immutable after creation; factory emits `RawSnapshotWritten`
- `SyncIncident`: OPEN→ESCALATED→AUTO_CLOSED/REOPENED; `recordFailure()`, `autoClose()`, `acknowledge()`

### Domain Events (10)
SyncStarted, SyncSucceeded, SyncFailed, RawSnapshotWritten, SyncIncidentOpened,
SyncIncidentEscalated, SyncIncidentAutoClosed, SyncIncidentReopened,
ManualTriggerRateLimited, AuthFailureDetected

### Repository Ports
SyncJobRepository, RawSnapshotRepository, SyncIncidentRepository

---

## Task 3 — Application Layer

Package: `com.derbysoft.click.modules.ingestion.application`

- `GoogleAdsReportingPort` — port for fetching campaign metrics
- `IngestionJobService` — enqueue daily/manual/backfill/forceRun, acknowledgeEscalation
- `JobExecutor` — acquire lease, fetch data, upsert rows, create snapshot, mark job
- `RetryPolicyEngine` — classify exceptions, compute exponential backoff delay
- `IncidentLifecycleService` — open/record/autoClose incidents
- `RateLimitService` — 3 manual triggers/hour per tenant
- `DailyIngestionScheduler` — @Scheduled cron, fires at UTC 02:00
- `JobRunner` — @Scheduled fixedDelay 30s, recover stuck + run pending
- `SyncRequestedListener` — @EventListener for BC4's SyncRequested event

---

## Task 4 — Infrastructure: Google Ads ACL

Package: `com.derbysoft.click.modules.ingestion.infrastructure.googleads`

- `IngestionFetchException` — TRANSIENT/PERMANENT with failureClass
- `IngestionAuthException` — always PERMANENT
- `GoogleAdsReportingClient` — implements GoogleAdsReportingPort; uses GoogleAdsConfig; GAQL for campaign metrics; maps auth/transient/permanent errors

---

## Task 5 — Infrastructure: Persistence

Package: `com.derbysoft.click.modules.ingestion.infrastructure.persistence`

### JPA Entities
- `SyncJobEntity` (sync_jobs)
- `RawSnapshotEntity` (raw_snapshots)
- `RawCampaignRowEntity` (raw_campaign_rows)
- `SyncIncidentEntity` (sync_incidents)

### JPA Repositories
- `SyncJobJpaRepository` — custom queries for pending/expired-lease/rate-limit
- `RawSnapshotJpaRepository`
- `RawCampaignRowJpaRepository` — native upsert ON CONFLICT
- `SyncIncidentJpaRepository`

### Mappers
SyncJobMapper, RawSnapshotMapper, SyncIncidentMapper

### Repository Implementations
- `SyncJobRepositoryImpl`
- `RawSnapshotRepositoryImpl`
- `SyncIncidentRepositoryImpl` — dual-interface (SyncIncidentRepository + IngestionQueryPort)

### Public API Contracts
- `SyncJobInfo` (record)
- `SyncIncidentInfo` (record)
- `IngestionQueryPort` (interface)

---

## Task 6 — Database Migrations

- `V202603050001__create_sync_jobs.sql`
- `V202603050002__create_raw_snapshots.sql`
- `V202603050003__create_sync_incidents.sql`

---

## Task 7 — Interfaces Layer

### SyncJobController `/api/v1/ingestion/jobs`
- POST /manual → 202
- POST /backfill → 202
- POST /force-run → 202
- GET /?integrationId → 200

### SyncIncidentController `/api/v1/ingestion/incidents`
- GET /?tenantId → 200
- GET /escalated?tenantId → 200
- POST /{id}/acknowledge → 200

### DTOs
ManualSyncRequest, BackfillRequest, ForceRunRequest, AcknowledgeRequest,
SyncJobResponse, SyncIncidentResponse

---

## Task 8 — Module Wiring

`ModuleRegistry.java` additions:
- `syncJobRepositoryImpl`
- `rawSnapshotRepositoryImpl`
- `syncIncidentRepositoryImpl` (dual-interface)

`GoogleAdsQueryPort` addition: `findAllActiveConnections()`
`GoogleConnectionRepositoryImpl` addition: implement `findAllActiveConnections()`

---

## Task 9 — Tests

### Unit Tests
- `SyncJobTest` (8 cases)
- `SyncIncidentTest` (6 cases)
- `IngestionJobServiceTest` (6 cases)
- `JobExecutorTest` (8 cases)
- `RetryPolicyEngineTest` (4 cases)

### Controller Tests (@WebMvcTest)
- `SyncJobControllerTest` (4 cases)
- `SyncIncidentControllerTest` (3 cases)

---

## Critical Files

| File | Action |
|------|--------|
| `ingestion/domain/aggregates/SyncJob.java` | Create |
| `ingestion/domain/aggregates/RawSnapshot.java` | Create |
| `ingestion/domain/aggregates/SyncIncident.java` | Create |
| `ingestion/domain/valueobjects/` (6 types) | Create |
| `ingestion/domain/events/` (10 records) | Create |
| `ingestion/domain/SyncJobRepository.java` | Create |
| `ingestion/domain/RawSnapshotRepository.java` | Create |
| `ingestion/domain/SyncIncidentRepository.java` | Create |
| `ingestion/application/ports/GoogleAdsReportingPort.java` | Create |
| `ingestion/application/handlers/IngestionJobService.java` | Create |
| `ingestion/application/handlers/JobExecutor.java` | Create |
| `ingestion/application/handlers/DailyIngestionScheduler.java` | Create |
| `ingestion/application/handlers/JobRunner.java` | Create |
| `ingestion/application/handlers/RetryPolicyEngine.java` | Create |
| `ingestion/application/handlers/IncidentLifecycleService.java` | Create |
| `ingestion/application/handlers/RateLimitService.java` | Create |
| `ingestion/application/handlers/SyncRequestedListener.java` | Create |
| `ingestion/infrastructure/googleads/GoogleAdsReportingClient.java` | Create |
| `ingestion/infrastructure/googleads/IngestionFetchException.java` | Create |
| `ingestion/infrastructure/googleads/IngestionAuthException.java` | Create |
| `ingestion/infrastructure/persistence/entity/` (4 entities) | Create |
| `ingestion/infrastructure/persistence/repository/` (4 JPA + 3 impl) | Create |
| `ingestion/infrastructure/persistence/mapper/` (3 mappers) | Create |
| `ingestion/api/contracts/SyncJobInfo.java` | Create |
| `ingestion/api/contracts/SyncIncidentInfo.java` | Create |
| `ingestion/api/ports/IngestionQueryPort.java` | Create |
| `ingestion/interfaces/http/controller/SyncJobController.java` | Create |
| `ingestion/interfaces/http/controller/SyncIncidentController.java` | Create |
| `ingestion/interfaces/http/dto/` (6 DTOs) | Create |
| `db/migration/V202603050001__create_sync_jobs.sql` | Create |
| `db/migration/V202603050002__create_raw_snapshots.sql` | Create |
| `db/migration/V202603050003__create_sync_incidents.sql` | Create |
| `bootstrap/di/ModuleRegistry.java` | Update |
| `googleadsmanagement/api/ports/GoogleAdsQueryPort.java` | Update |
| `googleadsmanagement/infrastructure/persistence/repository/GoogleConnectionRepositoryImpl.java` | Update |
| Test files (7 test classes, ~45 test cases) | Create |

---

## Verification

1. `./gradlew :apps:api:compileJava` — zero errors
2. `./gradlew :apps:api:test` — all new tests green (~45 new test cases)
3. `docker-compose up -d --build` — migrations V202603050001–3 applied cleanly
4. Login → get cookie token
5. Trigger manual sync: `POST /api/v1/ingestion/jobs/manual` → 202, job.status = PENDING
6. Wait ~30s for JobRunner tick: `GET /api/v1/ingestion/jobs/?integrationId=<id>` → job.status = SUCCEEDED
7. Verify: `SELECT * FROM raw_snapshots; SELECT * FROM raw_campaign_rows;`
8. Verify `RawSnapshotWritten` published in logs
9. Fire 4 manual syncs in < 1 hour → 4th returns error with `retryAfter`
10. Simulate failure → `GET /api/v1/ingestion/incidents?tenantId=<id>` shows `OPEN` incident
11. Fail 3× same key → incident status = `ESCALATED`
12. `POST /api/v1/ingestion/incidents/{id}/acknowledge` → 200
