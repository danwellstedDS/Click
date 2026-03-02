# BC4 Channel Integration — References

## Reference Patterns

| Pattern | File |
|---------|------|
| Aggregate | `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/domain/aggregates/PropertyGroup.java` |
| Repository impl | `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/infrastructure/persistence/repository/PropertyGroupRepositoryImpl.java` |
| Application handler | `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/application/handlers/PropertyManagementHandler.java` |
| REST controller | `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/interfaces/http/controller/PropertyManagementController.java` |
| Cross-BC API port | `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/api/ports/PropertyGroupQueryPort.java` |
| ModuleRegistry bean wiring | `apps/api/src/main/java/com/derbysoft/click/bootstrap/di/ModuleRegistry.java` |
| Controller test | `apps/api/src/test/java/com/derbysoft/click/modules/organisationstructure/interfaces/http/controller/PropertyManagementControllerTest.java` |
| BoundaryRulesTest | `apps/api/src/test/java/com/derbysoft/click/architecture/BoundaryRulesTest.java` |
| InProcessEventBus | `apps/api/src/main/java/com/derbysoft/click/bootstrap/messaging/InProcessEventBus.java` |
| EventEnvelope | `apps/api/src/main/java/com/derbysoft/click/sharedkernel/api/EventEnvelope.java` |
| DomainError | `apps/api/src/main/java/com/derbysoft/click/sharedkernel/domain/errors/DomainError.java` |
| ApiResponse | `apps/api/src/main/java/com/derbysoft/click/sharedkernel/api/ApiResponse.java` |
| ErrorHandlingAdvice | `apps/api/src/main/java/com/derbysoft/click/bootstrap/web/ErrorHandlingAdvice.java` |

## Related BCs

| BC | Package | Notes |
|----|---------|-------|
| BC1 | `com.derbysoft.click.modules.identityaccess` | JWT auth; `UserPrincipal` for security |
| BC2 | `com.derbysoft.click.modules.tenantgovernance` | Governance approval — accessed via `TenantGovernancePort` in `api/ports` |
| BC3 | `com.derbysoft.click.modules.organisationstructure` | Reference architecture pattern |
| BC7 | `com.derbysoft.click.modules.ingestion` | Will emit SyncSucceeded/SyncFailed (stub events defined in BC4 domain for now) |

## Flyway Migrations

Latest existing migration: `V202602250001__replace_chain_hotel_with_property_group.sql`
BC4 migration: `V202603020001__create_integration_instances.sql`
