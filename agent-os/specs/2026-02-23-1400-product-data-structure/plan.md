# Product Data Structure — Implementation Plan

## Context

Replacing the minimal auth-MVP schema (`tenants / users / tenant_memberships`) with the full product data structure: multi-org IAM, a chain/hotel property graph, access control via scopes and grants, and a billing layer.

**Chain = Tenant.** Access is determined entirely by `ScopeAccessGrant`; `tenant_memberships` is removed. Billing (`CustomerAccount / Contract / ContractCoverage`) is separate from authorization.

---

## Key Invariants

- Every operational record carries `chainId`
- Access resolved only through `ScopeAccessGrant` — never through `managed_by` fields
- Billing ≠ authorization
- `Role` enum kept (ADMIN / VIEWER) — still needed by JWT auth until auth is refactored
- `GrantRole` (VIEWER / ANALYST / MANAGER / ADMIN / SUPPORT) is the new access-level enum

---

## Files Modified / Created

### Migrations (`infra/db/migrations/`)
| File | Action |
|---|---|
| `V202602230001__replace_tenant_model.sql` | Drop tenant_memberships; rename tenants→chains; add timezone/currency cols; add name/is_active to users |
| `V202602230002__create_organizations.sql` | Create organizations; add primary_org_id FK to chains |
| `V202602230003__create_org_memberships.sql` | Create org_memberships |
| `V202602230004__create_hotels.sql` | Create hotels |
| `V202602230005__create_portfolios.sql` | Create portfolios + portfolio_hotels |
| `V202602230006__create_access_scopes.sql` | Create access_scopes with partial unique indexes |
| `V202602230007__create_scope_access_grants.sql` | Create scope_access_grants |
| `V202602230008__create_billing_tables.sql` | Create customer_accounts, contracts, contract_coverages |
| `V202602230009__update_seed.sql` | Replace old seed with new-model seed |

### Domain Layer (`libs/domain/`)
- **Remove**: `Tenant.java`, `TenantMembership.java`, `TenantMembershipRepository.java`
- **Update**: `User.java` (add name, isActive), `UserRepository.java` (remove findAllByTenantId)
- **New enums**: `OrganizationType`, `ScopeType`, `GrantRole`, `CustomerType`, `ContractStatus`
- **New models**: `Chain`, `Hotel`, `Organization`, `OrgMembership`, `Portfolio`, `PortfolioHotel`, `AccessScope`, `ScopeAccessGrant`, `CustomerAccount`, `Contract`, `ContractCoverage`
- **New repos**: `ChainRepository`, `HotelRepository`, `OrganizationRepository`, `OrgMembershipRepository`, `PortfolioRepository`, `AccessScopeRepository`, `ScopeAccessGrantRepository`, `CustomerAccountRepository`, `ContractRepository`

### Persistence Layer (`libs/persistence/`)
- **Remove**: `TenantEntity`, `TenantMembershipEntity`, `TenantMembershipJpaRepository`, `TenantMembershipRepositoryImpl`, `TenantMembershipMapper`
- **Update**: `UserEntity` (add name, isActive), `UserRepositoryImpl` (remove TenantMembership dep), `UserMapper` (map new fields)
- **New entities**: `ChainEntity`, `HotelEntity`, `OrganizationEntity`, `OrgMembershipEntity`, `PortfolioEntity`, `PortfolioHotelEntity`, `AccessScopeEntity`, `ScopeAccessGrantEntity`, `CustomerAccountEntity`, `ContractEntity`, `ContractCoverageEntity`
- **New JPA repos, impls, mappers** for each new entity

---

## Known Compile Failures (Expected)

`apps/api` will not compile after this task due to references to removed classes in `AuthService` and `UserManagementService`. These are fixed in the auth refactor task.

`libs/domain` and `libs/persistence` must compile clean.

---

## Verification

1. `./gradlew :libs:domain:build` — passes, no errors
2. `./gradlew :libs:persistence:build` — passes, no errors
3. Flyway applies all 9 migrations cleanly on fresh database
4. All tables present with correct columns and constraints
5. Seed data present: chain, org, user, org_membership, scope, grant
