# Structural References

## Existing Domain Model Patterns

**User** (`libs/domain/src/main/java/domain/User.java`)
- Full class pattern: private constructor, static `create(...)` factory, getters
- Fields: `id`, `email`, `passwordHash`, `createdAt`, `updatedAt`
- Follow this pattern for: `Chain`, `Hotel`, `Organization`, `Portfolio`, `CustomerAccount`, `Contract`

**TenantMembership** (`libs/domain/src/main/java/domain/TenantMembership.java`)
- Record pattern for simple value objects
- Follow this pattern for: `OrgMembership`, `PortfolioHotel`, `AccessScope`, `ScopeAccessGrant`, `ContractCoverage`

**Role** (`libs/domain/src/main/java/domain/Role.java`)
- Simple enum pattern
- Follow for: `OrganizationType`, `ScopeType`, `GrantRole`, `CustomerType`, `ContractStatus`

## Existing Persistence Patterns

**UserEntity** (`libs/persistence/src/main/java/persistence/entity/UserEntity.java`)
- `@Entity`, `@Table`, `@Id @GeneratedValue @UuidGenerator`, `@CreationTimestamp`, `@UpdateTimestamp`
- Protected no-arg constructor, public constructor for creation
- Follow for all new entities

**UserMapper** (`libs/persistence/src/main/java/persistence/mapper/UserMapper.java`)
- Static utility class, private constructor, `toDomain(Entity)` static method
- Follow for all new mappers

**UserRepositoryImpl** (`libs/persistence/src/main/java/persistence/repository/UserRepositoryImpl.java`)
- `@Repository`, constructor injection of JPA repo, delegates to mapper
- Follow for all new implementations

**UserJpaRepository** (`libs/persistence/src/main/java/persistence/repository/UserJpaRepository.java`)
- Extends `JpaRepository<Entity, UUID>`
- Follow for all new JPA repositories

## New Enum Reference Table

| Domain Enum | Values | Used In |
|---|---|---|
| `OrganizationType` | CHAIN, AGENCY, DERBYSOFT | `Organization.type`, `organizations.type` |
| `ScopeType` | CHAIN, HOTEL, PORTFOLIO | `AccessScope.type`, `access_scopes.type` |
| `GrantRole` | VIEWER, ANALYST, MANAGER, ADMIN, SUPPORT | `ScopeAccessGrant.role`, `scope_access_grants.role` |
| `CustomerType` | CHAIN_CUSTOMER, AGENCY_CUSTOMER | `CustomerAccount.type`, `customer_accounts.type` |
| `ContractStatus` | ACTIVE, PAUSED, CANCELLED | `Contract.status`, `contracts.status` |
| `Role` | ADMIN, VIEWER | JWT claims only — not stored in new tables |

## Seed UUIDs

For auth compatibility, the dev seed preserves the existing user UUID:
- Chain: `00000000-0000-0000-0000-000000000001`
- User: `00000000-0000-0000-0000-000000000002`
- Organization, OrgMembership, AccessScope, ScopeAccessGrant, Hotel: use `gen_random_uuid()`
