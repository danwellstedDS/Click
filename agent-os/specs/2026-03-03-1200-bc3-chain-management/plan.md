# BC3 Chain Management — Spec Plan

## Context

Hotel chains are the primary organisational unit in the platform — marketing teams manage paid search across a portfolio of properties grouped into "chains" (PropertyGroups). Currently there is no admin UI or structured API for creating or managing these chains; a Derbysoft Admin must interact with the DB directly.

This feature adds:
- A `status` field (ACTIVE | INACTIVE) to the `PropertyGroup` aggregate
- A `ChainManagementService` and REST controller (`/api/v1/chains`) for ADMIN-only CRUD
- A "Derbysoft Admin → Chain Management" section in the frontend sidebar with a list/add/edit-status UI

---

## Execution Order

### Task 1 — Save spec documentation

Create `agent-os/specs/2026-03-03-1200-bc3-chain-management/` containing:
- `plan.md` — this plan
- `shape.md` — shaping notes
- `standards.md` — relevant backend/frontend standards excerpts
- `references.md` — pointers to reference implementations

---

### Task 2 — Domain layer

**Path prefix:** `apps/api/src/main/java/com/derbysoft/click/modules/organisationstructure/domain/`

#### 2a — New `ChainStatus` enum

File: `valueobjects/ChainStatus.java`
```java
public enum ChainStatus { ACTIVE, INACTIVE }
```

#### 2b — New domain events

File: `events/ChainCreated.java`
```java
public record ChainCreated(UUID chainId, String name, Instant createdAt) {}
```

File: `events/ChainStatusChanged.java`
```java
public record ChainStatusChanged(UUID chainId, ChainStatus oldStatus, ChainStatus newStatus, Instant changedAt) {}
```

#### 2c — Update `PropertyGroup` aggregate

File: `aggregates/PropertyGroup.java`

Add:
- `private ChainStatus status` field (mutable)
- `status` param to private constructor and `reconstitute()` factory
- Update existing `create()` factory: status defaults to `ChainStatus.ACTIVE`; emit `ChainCreated` (alongside existing `OrgNodeCreated`)
- `reconstitute()` overload/update to include `ChainStatus status` param
- `activate()` method: if already ACTIVE throw `DomainError.Conflict`; set status, emit `ChainStatusChanged`
- `deactivate()` method: if already INACTIVE throw `DomainError.Conflict`; set status, emit `ChainStatusChanged`
- `getStatus()` accessor

#### 2d — Update `PropertyGroupRepository` domain interface

File: `PropertyGroupRepository.java`

Add:
```java
List<PropertyGroup> findAll();
PropertyGroup save(PropertyGroup chain);
```

---

### Task 3 — DB migration

File: `apps/api/src/main/resources/db/migration/V202603030003__add_property_group_status.sql`

```sql
ALTER TABLE property_groups
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
```

---

### Task 4 — Infrastructure layer

#### 4a — Update `PropertyGroupEntity`

Add `status` field, full-args constructor, getStatus/setStatus.

#### 4b — Update `PropertyGroupMapper`

Update `toDomain` to pass status. Add `toEntity(PropertyGroup domain)`.

#### 4c — Update `PropertyGroupRepositoryImpl`

Add `findAll()` and `save(PropertyGroup chain)` implementations.

---

### Task 5 — Application layer

**New file:** `application/handlers/ChainManagementService.java`

Methods:
- `listChains(UserPrincipal)` — requireAdmin; return `repository.findAll()`
- `createChain(String name, String timezone, String currency, UserPrincipal)` — requireAdmin; validate; save; publish event
- `updateStatus(UUID id, ChainStatus newStatus, UserPrincipal)` — requireAdmin; load; activate/deactivate; save
- `findById(UUID id, UserPrincipal)` — requireAdmin; load or throw

---

### Task 6 — Interfaces layer

DTOs: `CreateChainRequest`, `UpdateChainStatusRequest`, `ChainResponse`
Controller: `ChainManagementController` at `/api/v1/chains`

---

### Task 7 — Frontend

Feature module `apps/web/src/features/chains/` with types, API client, list page.
Update `AppLayout.tsx` and `App.tsx`.

---

### Task 8 — Tests

Backend unit and controller tests for the new service and controller.
