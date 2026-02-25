# PropertyGroup + Property Refactor — Standards

## Naming

- Java classes: `PropertyGroup`, `Property`, `PortfolioProperty`
- Table names: `property_groups`, `properties`, `portfolio_properties`
- Column names: `property_group_id`, `property_id`, `external_property_ref`
- Enum values: `PROPERTY_GROUP`, `PROPERTY` (uppercase snake for DB check constraint)

## Architecture Rules

- Domain layer has zero Spring/JPA dependencies.
- Repository interfaces are in `domain.repository`; implementations in `persistence.repository`.
- Mappers are stateless utility classes with a private constructor.
- No circular FK references: property_groups.parent_id is self-referential and uses ON DELETE CASCADE.

## Migration Standards

- Flyway: destructive migrations are acceptable in dev environment only.
- Drop tables in reverse FK dependency order before recreating.
- Seed data uses deterministic UUIDs (all-zero prefix) for reproducibility.
- Each seed migration replaces the previous seed completely.

## Test Standards

- Unit tests use Mockito; mock only direct dependencies of the class under test.
- AuthServiceTest mocks: UserRepository, OrgMembershipRepository, PropertyGroupRepository, RefreshTokenRepository, JwtService, PasswordEncoder.
