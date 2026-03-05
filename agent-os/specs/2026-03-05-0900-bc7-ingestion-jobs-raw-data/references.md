# BC7 Ingestion — References

## Key Reference Files

### BC4 (Channel Integration) — Scheduler Pattern
- `ScheduleService.java`: `@Scheduled(fixedDelay = 60_000)`, per-item exception swallow, `isDue()` guard
  - Path: `apps/api/src/main/java/com/derbysoft/click/modules/channelintegration/application/handlers/ScheduleService.java`

### BC4 (Channel Integration) — Integration Service Pattern
- `IntegrationService.java`: `@Service @Transactional`, `publishAndClear()` pattern, `requireById()` helper
  - Path: `apps/api/src/main/java/com/derbysoft/click/modules/channelintegration/application/handlers/IntegrationService.java`

### BC5 (Google Ads Management) — Google Ads Client
- `GoogleAdsApiClient.java`: `buildClient()` helper, `GoogleAdsClient.newBuilder()`, credential loading, error handling
  - Path: `apps/api/src/main/java/com/derbysoft/click/modules/googleadsmanagement/infrastructure/googleads/GoogleAdsApiClient.java`
- `GoogleAdsConfig.java`: `getCredentialsPath()`, `getDeveloperToken()`
  - Path: `apps/api/src/main/java/com/derbysoft/click/modules/googleadsmanagement/infrastructure/googleads/GoogleAdsConfig.java`

### BC5 (Google Ads Management) — Dual-Interface Repository
- `GoogleConnectionRepositoryImpl.java`: implements domain port + public query port, mapper pattern
  - Path: `apps/api/src/main/java/com/derbysoft/click/modules/googleadsmanagement/infrastructure/persistence/repository/GoogleConnectionRepositoryImpl.java`

### BC5 (Google Ads Management) — DiscoverAccountsHandler
- Shows how to load connection, call API, handle failures, publish events
  - Path: `apps/api/src/main/java/com/derbysoft/click/modules/googleadsmanagement/application/handlers/DiscoverAccountsHandler.java`

### Shared Kernel
- `DomainError.java`: `NotFound`, `ValidationError`, `Conflict`, `Unauthenticated`, `Forbidden`
  - Path: `apps/api/src/main/java/com/derbysoft/click/sharedkernel/domain/errors/DomainError.java`
- `ApiResponse.java`: `success(data, requestId)`, `error(code, message, requestId)`
  - Path: `apps/api/src/main/java/com/derbysoft/click/sharedkernel/api/ApiResponse.java`
- `EventEnvelope.java`: `of(eventType, payload)`, `ResolvableTypeProvider` for Spring routing
  - Path: `apps/api/src/main/java/com/derbysoft/click/sharedkernel/api/EventEnvelope.java`
- `InProcessEventBus.java`: wraps `ApplicationEventPublisher`
  - Path: `apps/api/src/main/java/com/derbysoft/click/bootstrap/messaging/InProcessEventBus.java`

### Bootstrap
- `ModuleRegistry.java`: `@Configuration`, explicit `@Bean` declarations for cross-BC wiring
  - Path: `apps/api/src/main/java/com/derbysoft/click/bootstrap/di/ModuleRegistry.java`

## BC5 Public Contracts Used by BC7

### GoogleAdsQueryPort (extended for BC7)
- `findConnectionByTenantId(UUID tenantId)` → `Optional<GoogleAdsConnectionInfo>`
- `listActiveBindings(UUID tenantId)` → `List<AccountBindingInfo>`
- `findAllActiveConnections()` → `List<GoogleAdsConnectionInfo>` (added for BC7 scheduler)

### AccountBindingInfo
```java
record AccountBindingInfo(UUID id, UUID tenantId, String customerId, String status, String bindingType)
```
Status values: ACTIVE, STALE, BROKEN, REMOVED
BC7 processes ACTIVE and STALE; skips BROKEN and REMOVED.

### GoogleAdsConnectionInfo
```java
record GoogleAdsConnectionInfo(UUID id, UUID tenantId, String managerId, String status)
```

## SyncRequested Event (BC4 → BC7)
```java
// channelintegration/domain/events/SyncRequested.java
record SyncRequested(UUID integrationId, UUID tenantId, Channel channel,
                     CredentialRef credentialRef, UUID syncRunId, Instant occurredAt)
```
BC7 listens to this event and calls `enqueueDailySync(integrationId, tenantId)`.

## Database Migration Reference
Migrations in: `apps/api/src/main/resources/db/migration/`
Latest BC5 migrations: `V202603040001` through `V202603040004`
BC7 migrations start at: `V202603050001`
