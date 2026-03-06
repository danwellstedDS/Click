# Standards Applied — BC6

## Module Structure
Hexagonal DDD: domain → application → infrastructure → interfaces. No Spring annotations in domain.

## Aggregate Pattern
Private constructor + static `create(...)` + static `reconstitute(...)`. `List<Object> events`
accumulated per operation, published by application service, cleared after.

## Repository Pattern
Domain interface in `domain/`. JPA entity + JPA repository + mapper + impl in `infrastructure/`.
Impl wired as `@Bean` in `ModuleRegistry` (not `@Repository`).

## Dual-Interface Pattern
`WriteActionRepositoryImpl` implements both `WriteActionRepository` (domain port) and
`CampaignManagementQueryPort` (public API port). Same pattern as BC7's `SyncIncidentRepositoryImpl`.

## API Contracts
All response records. Request records with Jakarta validation. Controllers extract tenantId from
`request.getAttribute("tenantId")`. Wrap with `ApiResponse.success(...)`.

## Tests
Domain: pure unit, no Spring. Application: `@ExtendWith(MockitoExtension.class)`.
Controller: `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters=false)` + `@MockitoBean`.
