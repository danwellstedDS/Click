# References — BD Alignment BC1/BC2/BC3

## Reference Implementation: BC4 (Channel Integration)

BC4 is the canonical reference for DDD patterns in this codebase.

### Domain event accumulation on aggregate
`modules/channelintegration/domain/aggregates/IntegrationInstance.java`
- `private final List<Object> events = new ArrayList<>()`
- `create(...)` factory emits `IntegrationCreated`
- `reconstitute(...)` does NOT emit events
- `getEvents()` returns unmodifiable list
- `clearEvents()` empties the list

### Event publishing in application handler
`modules/channelintegration/application/handlers/IntegrationService.java`
- Calls `repository.save(instance)` first
- Then calls `publishAndClear(saved)` which iterates `saved.getEvents()`
- Uses `InProcessEventBus.publish(EventEnvelope.of(...))`

### Cross-BC API port pattern
`modules/organisationstructure/api/ports/PropertyGroupQueryPort.java`
- Only `api/` package is importable by other BCs
- Returns DTO contracts from `api/contracts/`
- Never exposes domain types across BC boundaries

### Record value object
`modules/channelintegration/domain/valueobjects/CredentialRef.java`
- Plain Java `record`
- No Spring/JPA annotations
- Immutable by definition

## Critical Files Changed in This Spec

| File | Change |
|------|--------|
| `identityaccess/domain/entities/OrgMembership.java` | Deleted |
| `identityaccess/domain/entities/TenantMembership.java` | Created |
| `identityaccess/domain/valueobjects/AuthClaims.java` | Deleted |
| `identityaccess/domain/valueobjects/ActorContext.java` | Created |
| `identityaccess/domain/valueobjects/Role.java` | Updated (5 values) |
| `identityaccess/domain/events/` | Created (3 events) |
| `identityaccess/domain/aggregates/User.java` | Updated (event capture) |
| `identityaccess/domain/OrgMembershipRepository.java` | Deleted |
| `identityaccess/domain/TenantMembershipRepository.java` | Created |
| `identityaccess/infrastructure/persistence/entity/OrgMembershipEntity.java` | Replaced by TenantMembershipEntity |
| `identityaccess/infrastructure/persistence/mapper/OrgMembershipMapper.java` | Replaced by TenantMembershipMapper |
| `identityaccess/infrastructure/persistence/repository/OrgMembership*.java` | Replaced by TenantMembership* |
| `identityaccess/application/handlers/AuthCommandHandler.java` | Updated |
| `identityaccess/application/handlers/UserManagementHandler.java` | Updated |
| `identityaccess/infrastructure/security/JwtService.java` | Updated |
| `identityaccess/infrastructure/security/JwtAuthFilter.java` | Updated |
| `db/migration/V202603020001__align_bc1_tenant_membership.sql` | Created |
| `organisationstructure/domain/events/` | Created (3 events) |
| `organisationstructure/domain/aggregates/PropertyGroup.java` | Updated (event capture) |
| `organisationstructure/application/handlers/PropertyManagementHandler.java` | Updated |
| `organisationstructure/api/ports/PropertyGroupQueryPort.java` | Updated (remove findInfoByPrimaryOrgId) |
| `bootstrap/di/ModuleRegistry.java` | Updated (remove PropertyGroupQueryPort injection to AuthCommandHandler) |
| `architecture/BoundaryRulesTest.java` | Updated (add BC1 domain isolation rule) |
