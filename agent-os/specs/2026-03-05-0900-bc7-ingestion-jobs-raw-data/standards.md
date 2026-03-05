# BC7 Ingestion — Standards Reference

## Backend Standards

### Package Structure
All BC7 code lives under: `com.derbysoft.click.modules.ingestion`

Sub-packages:
- `domain/aggregates/` — Aggregate roots (SyncJob, RawSnapshot, SyncIncident)
- `domain/valueobjects/` — Value objects and enums
- `domain/events/` — Domain events (records)
- `application/handlers/` — Application services and schedulers
- `application/ports/` — Application-layer ports (GoogleAdsReportingPort)
- `infrastructure/googleads/` — Google Ads API adapter (ACL)
- `infrastructure/persistence/entity/` — JPA entities
- `infrastructure/persistence/mapper/` — Domain↔Entity mappers
- `infrastructure/persistence/repository/` — JPA Spring Data repos + impls
- `api/contracts/` — Public query result types
- `api/ports/` — Public query ports (IngestionQueryPort)
- `interfaces/http/controller/` — REST controllers
- `interfaces/http/dto/` — Request/response DTOs

### Aggregate Pattern
- Aggregates hold `List<Object> events` (private, not exposed directly)
- Factory method `create(...)` emits domain events
- `reconstitute(...)` factory does NOT emit events (for loading from DB)
- `getEvents()` returns unmodifiable list
- `clearEvents()` empties the list
- Application services call `publishAndClear(aggregate)` after save

### Event Publishing
```java
private void publishAndClear(SyncJob job) {
    job.getEvents().forEach(event ->
        eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
    );
    job.clearEvents();
}
```

### Repository Pattern
- Domain port interface in `domain/`
- JPA Spring Data interface in `infrastructure/persistence/repository/`
- Implementation class in `infrastructure/persistence/repository/`
- Mapper component in `infrastructure/persistence/mapper/`

### Dual-Interface Repository
`SyncIncidentRepositoryImpl` implements both:
- `SyncIncidentRepository` (domain port)
- `IngestionQueryPort` (public API port)
Registered explicitly in `ModuleRegistry` (not via `@Repository`)

### Wiring
All cross-BC beans registered in `ModuleRegistry.java` via `@Configuration @Bean`.
Do not use `@Repository` or `@Component` on implementations that implement multiple interfaces.

## API Spec Standards

### Response Wrapper
All HTTP responses wrapped in `ApiResponse<T>`:
```java
ApiResponse.success(data, requestId(httpRequest))
```

### Request IDs
```java
private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute("requestId");
    return value == null ? "unknown" : value.toString();
}
```

### HTTP Status Codes
- `202 Accepted` — async job enqueued
- `200 OK` — read operations, acknowledge
- `404 Not Found` — DomainError.NotFound
- `409 Conflict` — DomainError.Conflict
- `400 Bad Request` — DomainError.ValidationError

### Validation
Use Jakarta Validation annotations on DTOs: `@NotNull`, `@NotBlank`

## Workflow Standards

### Idempotency Guards
Always check for existing PENDING/RUNNING job before creating new one.

### Transaction Boundaries
`@Transactional` at the service method level. One transaction per use case.

### Scheduled Tasks
Use `@Scheduled` with `fixedDelay` or `cron`. Swallow per-item exceptions with `log.warn`.

### Cross-BC Dependencies
- Only import from other BCs' `api/` packages (contracts, events, ports)
- Never import from another BC's `domain/`, `application/`, or `infrastructure/`
- Exception: BC7 application layer may reference BC5 `infrastructure.googleads.GoogleAdsConfig` for credential path
