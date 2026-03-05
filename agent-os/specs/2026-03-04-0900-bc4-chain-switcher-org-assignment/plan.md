# BC4 Chain Context Switcher + Org Assignment — Spec Plan

## Context

BC3 delivered chain CRUD and the admin UI. Two gaps remain:

1. **Chain context switcher** — the AppLayout header already has an empty `<Dropdown>` placeholder showing `user?.tenantName`. It needs to be populated with the chains a user can access, so they can switch context (re-issue a JWT with a different `tenantId`).
2. **Org assignment on creation** — when a Derbysoft Admin creates a chain, they should be able to assign it to an organisation. This sets `primary_org_id` on the PropertyGroup, creates the access scaffolding (`AccessScope`, `ScopeAccessGrant`), and creates a `TenantMembership` for the creator so they can immediately switch to the new chain.

---

## Execution Order

### Task 1 — Save spec documentation

Create `agent-os/specs/2026-03-04-0900-bc4-chain-switcher-org-assignment/` with:
- `plan.md` — this full plan
- `shape.md` — shaping notes and decisions
- `standards.md` — relevant standards excerpts
- `references.md` — reference implementation pointers

---

### Task 2 — Backend: List organizations endpoint

There is currently no `GET /api/v1/organizations` endpoint. `OrganizationRepository` only has `findById` and `create`.

#### 2a — Add `findAll()` to `OrganizationRepository`

File: `modules/tenantgovernance/domain/OrganizationRepository.java`
```java
List<Organization> findAll();
```

#### 2b — Implement `findAll()` in `OrganizationRepositoryImpl`

File: `modules/tenantgovernance/infrastructure/persistence/repository/OrganizationRepositoryImpl.java`

Delegate to `organizationJpaRepository.findAll().stream().map(OrganizationMapper::toDomain).toList()`.

#### 2c — New `OrganizationManagementController`

File: `modules/tenantgovernance/interfaces/http/controller/OrganizationManagementController.java`

```
GET /api/v1/organizations → 200 ApiResponse<List<OrgResponse>>
```

- ADMIN-only (requireAdmin check via `UserPrincipal`)
- Delegates to a thin `OrganizationQueryService` or inline service call to `organizationRepository.findAll()`

New DTO: `modules/tenantgovernance/interfaces/http/dto/OrgResponse.java`
```java
public record OrgResponse(UUID id, String name, String type) {}
```

---

### Task 3 — Backend: Accessible tenants endpoint

The chain switcher needs chain names alongside IDs. `TenantMembership` only stores `tenantId` (UUID). A dedicated endpoint resolves names.

#### 3a — Add `GET /api/v1/auth/tenants` to `AuthController`

File: `modules/identityaccess/interfaces/http/controller/AuthController.java` (update existing)

```
GET /api/v1/auth/tenants → 200 ApiResponse<List<TenantSummary>>
```

New DTO: `modules/identityaccess/interfaces/http/dto/TenantSummary.java`
```java
public record TenantSummary(String tenantId, String tenantName, String role) {}
```

New method in `AuthCommandHandler`:
```java
public List<TenantSummary> listTenants(UserPrincipal principal) {
    return tenantMembershipRepository.findByUserId(principal.userId())
        .stream()
        .map(m -> {
            String name = propertyGroupQueryPort.findInfoById(m.tenantId())
                .map(PropertyGroupInfo::name)
                .orElse(m.tenantId().toString());
            return new TenantSummary(m.tenantId().toString(), name, m.role().name());
        })
        .toList();
}
```

---

### Task 4 — Backend: Org assignment on chain creation

#### 4a — Update `CreateChainRequest`

File: `modules/organisationstructure/interfaces/http/dto/CreateChainRequest.java`

Add optional field:
```java
UUID organizationId   // nullable — no @NotNull
```

#### 4b — Update `ChainManagementService.createChain()`

File: `modules/organisationstructure/application/handlers/ChainManagementService.java`

New signature:
```java
public PropertyGroup createChain(
    String name, String timezone, String currency,
    UUID organizationId,   // nullable
    UserPrincipal principal
)
```

Pass `organizationId` to `PropertyGroup.create()` so it's persisted as `primary_org_id` on the initial INSERT:
```java
PropertyGroup saved = repository.save(
    PropertyGroup.create(null, null, name, timezone, currency, organizationId, now, now)
);
```

After `repository.save(...)`:

1. **Always** create a `TenantMembership` for the creator (so they can switch to the new chain immediately):
   ```java
   tenantMembershipRepository.create(UUID.randomUUID(), principal.userId(), saved.getId(), Role.ADMIN);
   ```

2. **If `organizationId` is non-null**, create access scaffolding:
   ```java
   AccessScope scope = accessScopeRepository.create(ScopeType.PROPERTY_GROUP, saved.getId(), null, null);
   scopeAccessGrantRepository.create(organizationId, scope.id(), GrantRole.ADMIN);
   ```

#### 4c — Update `ChainManagementController`

File: `modules/organisationstructure/interfaces/http/controller/ChainManagementController.java`

Pass `request.organizationId()` through to the service.

#### 4d — Inject new repositories into `ChainManagementService`

Add constructor params:
- `AccessScopeRepository accessScopeRepository`
- `ScopeAccessGrantRepository scopeAccessGrantRepository`
- `TenantMembershipRepository tenantMembershipRepository`

These are already registered as Spring beans — no new wiring needed.

---

### Task 5 — Frontend: Org select in Create Chain modal

#### 5a — New `organizationsApi.ts`

File: `apps/web/src/features/chains/organizationsApi.ts`
```typescript
import { apiRequest } from "../../lib/apiClient";

export type OrgOption = { id: string; name: string; type: string };

export function list(): Promise<OrgOption[]> {
  return apiRequest<OrgOption[]>("/api/v1/organizations");
}
```

#### 5b — Update `ChainsListPage.tsx`

File: `apps/web/src/features/chains/ChainsListPage.tsx`

- Load `orgs` list on mount via `organizationsApi.list()`
- Add `organizationId` state to the create form
- In the "Add Chain" modal, add an Ant Design `<Select>` for Organisation (optional, placeholder "Select organisation (optional)")
- Pass `organizationId` in the `chainsApi.create()` call

#### 5c — Update `chainsApi.ts`

File: `apps/web/src/features/chains/chainsApi.ts`

```typescript
export function create(data: {
  name: string;
  timezone?: string;
  currency?: string;
  organizationId?: string;
}): Promise<Chain>
```

---

### Task 6 — Frontend: Chain switcher in AppLayout

#### 6a — Update `authApi.ts`

File: `apps/web/src/features/auth/authApi.ts`

Add:
```typescript
export type TenantSummary = { tenantId: string; tenantName: string; role: string };

export function listTenants(): Promise<TenantSummary[]> {
  return apiRequest<TenantSummary[]>("/api/v1/auth/tenants");
}
```

#### 6b — Update `AuthContext.tsx`

File: `apps/web/src/features/auth/AuthContext.tsx`

- Add `tenants: TenantSummary[]` to context state (populated after login / on mount alongside `me()`)
- Call `authApi.listTenants()` to populate and expose via context

#### 6c — Update `AppLayout.tsx`

File: `apps/web/src/components/AppLayout.tsx`

Replace `items: []` with items derived from `tenants`:
```tsx
const items: MenuProps["items"] = tenants.map(t => ({
  key: t.tenantId,
  label: t.tenantName,
  icon: t.tenantId === user?.tenantId ? <CheckOutlined /> : null,
  onClick: () => switchTenant(t.tenantId),
}));
```

---

### Task 7 — Tests

#### Backend unit tests

**`ChainManagementServiceTest.java`** (add cases):
- `shouldCreateTenantMembershipForCreatorOnChainCreation`
- `shouldCreateAccessScopeAndGrantWhenOrgIdProvided`
- `shouldNotCreateScopeGrantWhenOrgIdNull`

**`AuthCommandHandlerTest.java`** (add):
- `shouldReturnTenantSummaryListForCurrentUser`
- `shouldResolveChainNameInTenantSummary`

**`OrganizationManagementControllerTest.java`** (new, `@WebMvcTest`):
- `shouldReturn200WithOrganisationList`
- `shouldReturn403WhenNotAdmin`

---

## Critical Files

| File | Action |
|------|--------|
| `tenantgovernance/domain/OrganizationRepository.java` | Update — add `findAll()` |
| `tenantgovernance/infrastructure/persistence/repository/OrganizationRepositoryImpl.java` | Update — implement `findAll()` |
| `tenantgovernance/interfaces/http/controller/OrganizationManagementController.java` | Create |
| `tenantgovernance/interfaces/http/dto/OrgResponse.java` | Create |
| `identityaccess/interfaces/http/controller/AuthController.java` | Update — add `GET /api/v1/auth/tenants` |
| `identityaccess/interfaces/http/dto/TenantSummary.java` | Create |
| `identityaccess/application/handlers/AuthCommandHandler.java` | Update — add `listTenants()` |
| `organisationstructure/interfaces/http/dto/CreateChainRequest.java` | Update — add `organizationId` |
| `organisationstructure/application/handlers/ChainManagementService.java` | Update — org assignment + creator membership |
| `organisationstructure/interfaces/http/controller/ChainManagementController.java` | Update — pass `organizationId` |
| `apps/web/src/features/chains/organizationsApi.ts` | Create |
| `apps/web/src/features/chains/ChainsListPage.tsx` | Update — org select in modal |
| `apps/web/src/features/chains/chainsApi.ts` | Update — add `organizationId` |
| `apps/web/src/features/auth/authApi.ts` | Update — add `listTenants()` |
| `apps/web/src/features/auth/AuthContext.tsx` | Update — load + expose `tenants` |
| `apps/web/src/components/AppLayout.tsx` | Update — populate chain switcher dropdown |

---

## Reference Patterns

| Pattern | File |
|---------|------|
| `TenantMembership` creation | `TenantMembershipRepositoryImpl.java` — `create(membershipId, userId, tenantId, role)` |
| `AccessScope` + `ScopeAccessGrant` creation | `AccessScopeRepositoryImpl.java`, `ScopeAccessGrantRepositoryImpl.java` |
| `switchTenant` / JWT re-issue | `AuthCommandHandler.switchTenant()` |
| `me()` tenant name resolution (same pattern as `listTenants`) | `AuthCommandHandler.me()` |
| ADMIN-only controller | `ChainManagementController.java` — `@AuthenticationPrincipal UserPrincipal` |
| `ApiResponse<T>` wrapper | `ChainManagementController.java` — `ApiResponse.success(data)` |
| Chain switcher placeholder | `AppLayout.tsx` lines 114–128 — `<Dropdown menu={{ items: [] }}>` |
| `AuthContext.switchTenant()` | `apps/web/src/features/auth/AuthContext.tsx` |

---

## Verification

1. `./gradlew :apps:api:compileJava` — zero errors
2. `./gradlew :apps:api:test` — all new tests green
3. `GET /api/v1/organizations` (as ADMIN) → list of orgs with id, name, type
4. `POST /api/v1/chains` with `{ "name": "Test", "organizationId": "<orgId>" }` → 201; confirm DB: `tenant_memberships` has a row for the creator, `access_scopes` has a PROPERTY_GROUP row, `scope_access_grants` has a row for the org
5. `GET /api/v1/auth/tenants` → includes the newly created chain in the list for the admin user
6. `POST /api/v1/auth/switch-tenant` with the new chain id → 200 with new JWT
7. Frontend: Create Chain modal shows "Organisation" select populated with orgs from the API
8. Frontend: AppLayout header dropdown lists accessible chains; clicking one re-issues the JWT and updates `user.tenantName`
