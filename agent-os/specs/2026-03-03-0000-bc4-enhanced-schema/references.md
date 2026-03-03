# BC4 Enhanced Schema — References

## Existing BC4 Files

| File | Role |
|------|------|
| `domain/valueobjects/SyncSchedule.java` | Replaced — cadence-aware version |
| `domain/valueobjects/IntegrationHealth.java` | Replaced — full outcome fields |
| `domain/aggregates/IntegrationInstance.java` | Updated — connectionKey, health persistence |
| `domain/IntegrationInstanceRepository.java` | Updated — findAllSchedulable |
| `infrastructure/persistence/entity/IntegrationInstanceEntity.java` | Updated — all new columns |
| `infrastructure/persistence/mapper/IntegrationInstanceMapper.java` | Updated — full field mapping |
| `infrastructure/persistence/repository/IntegrationInstanceJpaRepository.java` | Updated — new queries |
| `infrastructure/persistence/repository/IntegrationInstanceRepositoryImpl.java` | Updated |
| `application/handlers/IntegrationService.java` | Updated — connectionKey, actorId, health |
| `application/handlers/ScheduleService.java` | Updated — cadence-aware triggering |
| `interfaces/http/dto/CreateIntegrationRequest.java` | Updated — cadence fields |
| `interfaces/http/dto/UpdateScheduleRequest.java` | Updated — cadence fields |
| `interfaces/http/dto/IntegrationInstanceResponse.java` | Updated — all new fields |
| `interfaces/http/controller/IntegrationManagementController.java` | Updated — actorId, connectionKey |

## New Files Created

| File | Role |
|------|------|
| `domain/valueobjects/CadenceType.java` | Enum: MANUAL, CRON, INTERVAL |
| `domain/valueobjects/SyncStatus.java` | Enum: NEVER, SUCCESS, FAILED |
| `db/migration/V202603030001__enhance_integration_instances.sql` | Schema migration |
| `db/migration/V202603030002__seed_integration_instances.sql` | Demo seed data |

## Prior Spec

`agent-os/specs/2026-03-02-0900-channel-integration-bc4/` — Initial BC4 implementation

## Reference Patterns

| Pattern | Location |
|---------|----------|
| Sealed status value object | `domain/valueobjects/IntegrationStatus.java` |
| Aggregate event emission | `domain/aggregates/IntegrationInstance.java` |
| Mapper pattern | `infrastructure/persistence/mapper/IntegrationInstanceMapper.java` |
| Spring CronExpression | `org.springframework.scheduling.support.CronExpression` (stdlib) |
