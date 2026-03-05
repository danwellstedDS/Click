# BC4 Reference Implementation Pointers

## Backend References

| What | Where |
|------|-------|
| `TenantMembership.create()` signature | `TenantMembershipRepository.java` → `create(UUID membershipId, UUID userId, UUID tenantId, Role role)` |
| `TenantMembershipRepositoryImpl` | `identityaccess/infrastructure/persistence/repository/TenantMembershipRepositoryImpl.java` |
| `AccessScope.create()` signature | `AccessScopeRepository.java` → `create(ScopeType type, UUID propertyGroupId, UUID propertyId, UUID portfolioId)` |
| `AccessScopeRepositoryImpl` | `tenantgovernance/infrastructure/persistence/repository/AccessScopeRepositoryImpl.java` |
| `ScopeAccessGrant.create()` signature | `ScopeAccessGrantRepository.java` → `create(UUID organizationId, UUID scopeId, GrantRole role)` |
| `ScopeAccessGrantRepositoryImpl` | `tenantgovernance/infrastructure/persistence/repository/ScopeAccessGrantRepositoryImpl.java` |
| `switchTenant()` (JWT re-issue) | `AuthCommandHandler.java:switchTenant()` |
| `me()` tenant name resolution | `AuthCommandHandler.java:me()` — uses `propertyGroupQueryPort.findInfoById()` |
| ADMIN-only guard | `ChainManagementService.java:requireAdmin()` |
| `ApiResponse.success()` | `ChainManagementController.java` — `ApiResponse.success(data, requestId)` |
| `PropertyGroup.create()` factory | `PropertyGroup.java:create(UUID id, UUID parentId, String name, String timezone, String currency, UUID primaryOrgId, Instant createdAt, Instant updatedAt)` |
| `OrganizationMapper.toDomain()` | `tenantgovernance/infrastructure/persistence/mapper/OrganizationMapper.java` |
| `Organization` domain record | `tenantgovernance/domain/aggregates/Organization.java` — fields: `id`, `name`, `type` |
| `OrganizationType` enum | `tenantgovernance/domain/valueobjects/OrganizationType.java` |

## Frontend References

| What | Where |
|------|-------|
| Chain switcher dropdown placeholder | `AppLayout.tsx` — `<Dropdown menu={{ items: [] }} trigger={["click"]}>` around the BankOutlined button |
| `switchTenant()` in context | `AuthContext.tsx:switchTenant()` — calls `authApi.switchTenant(tenantId)` then refreshes `me()` |
| `useAuth()` hook | `AuthContext.tsx` — exposes `{ user, tenants, isLoading, login, logout, switchTenant }` |
| `apiRequest` client | `lib/apiClient.ts` |
| Existing `chainsApi.create()` | `features/chains/chainsApi.ts` |
| Org select component | Use `<Select>` from `@derbysoft/neat-design` inside a `<Form.Item>` |

## Test References

| What | Where |
|------|-------|
| Existing `ChainManagementServiceTest` | `organisationstructure/application/handlers/ChainManagementServiceTest.java` |
| Existing `AuthCommandHandlerTest` | `identityaccess/application/handlers/AuthCommandHandlerTest.java` |
| `@WebMvcTest` controller test example | Any `*ControllerTest.java` in the project |
| Mock pattern | Mockito `@Mock` / `@InjectMocks` / `when(...).thenReturn(...)` |
