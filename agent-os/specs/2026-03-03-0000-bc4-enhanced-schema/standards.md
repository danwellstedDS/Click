# BC4 Enhanced Schema — Standards Applied

## Domain Layer Standards

### Value Object Design
- Records are immutable; mutation produces a new instance (withSuccess, withFailure)
- Compact constructors validate all invariants eagerly
- Static factory methods express intent (manual/cron/interval vs raw constructor)

### Aggregate Design
- `connectionKey` is immutable (set at `create()`, never changed)
- `credentialAttachedAt` and `updatedBy` are set on each mutating operation
- `health` defaults to `IntegrationHealth.empty()` — never null after `create()`
- `reconstitute()` signature extended to include all new fields

## Infrastructure Standards

### JPA Entity
- Mutable fields have setters; immutable fields (`id`, `tenantId`, `channel`, `connectionKey`, `createdAt`) are constructor-only
- `@Column(updatable = false)` on immutable fields
- New timestamp fields use `Instant` (TIMESTAMPTZ in Postgres)

### Mapper
- `toDomain`: constructs `SyncSchedule` via the appropriate static factory based on `cadenceType` column value
- `toEntity`: writes all fields; `cron_expression` and `interval_minutes` are nullable per cadence type

### Repository
- `findByTenantIdAndChannelAndConnectionKey` replaces `findByTenantIdAndChannel`
- `findAllSchedulable` returns only Active, non-MANUAL instances for scheduler

## DB Migration Standards

### Safe Migration Pattern
1. Add all new columns as nullable
2. Backfill data from old columns
3. Apply NOT NULL constraints
4. Drop old columns
5. Add new constraints

### Flyway Naming
`V{yyyyMMddHHmm}__{description}.sql` — version prefix uses date+time to ensure ordering

## Testing Standards

### Domain Unit Tests
- Pure Java, no Spring context, no mocks
- Assert on exact health field values (not just non-null)
- Test all three cadence validation paths

### Application Service Tests (ScheduleService)
- Mockito `@ExtendWith(MockitoExtension.class)`
- Test MANUAL skip, INTERVAL timing, CRON trigger

## Spring CronExpression Usage

`org.springframework.scheduling.support.CronExpression` is available on the classpath via `spring-context`. No new dependency required. Usage:

```java
CronExpression expr = CronExpression.parse(cronExpression);
LocalDateTime lastCheck = lastSyncAt.atZone(ZoneId.of(timezone)).toLocalDateTime();
LocalDateTime nextRun = expr.next(lastCheck);
boolean isDue = nextRun == null || !nextRun.isAfter(LocalDateTime.now(ZoneId.of(timezone)));
```
