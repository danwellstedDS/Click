# Property Admin Area — Plan

## Context

The `PropertyGroup`/`Property` data model is now fully implemented and migrated. The Properties nav item exists in the sidebar but has no route or page. This plan wires it up: a table of active properties scoped to the currently-selected PropertyGroup (the `tenantId` from `UserPrincipal`), with Create and soft-Delete actions, and a "View" stub that navigates to a future property profile page (`/properties/:id`).

Unused nav items (Dashboard sub-items, Reporting, Bidding, Budgets, Content, Feed manager) have no routes and will be removed to reduce noise.

**Key decisions:**
- Delete = soft delete (`is_active = false`); table only shows active properties
- Create form fields: Name (required), Is Active toggle (default true), External Property Ref (optional)
- Update/profile page is out of scope — "View" button navigates but no page yet
- PropertyGroup context comes from `principal.tenantId()` (already set during login)

---

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-02-25-1430-property-admin-area/` with:
- **plan.md** — this plan
- **shape.md** — shaping decisions and context
- **standards.md** — backend, frontend, workflow, documentation standards content
- **references.md** — pointers to the Users feature as reference implementation

---

## Task 2: Domain + Persistence Layer

### `libs/domain/src/main/java/domain/repository/PropertyRepository.java`
Extend the interface with full-field `create` and `deactivate`:

```java
Property create(UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef);
void deactivate(UUID id);
```

(Current `create(UUID, String)` is replaced by the 4-arg version.)

### `libs/persistence/src/main/java/persistence/entity/PropertyEntity.java`
Add a constructor accepting all four fields so the existing JPA no-arg constructor is preserved:

```java
public PropertyEntity(UUID propertyGroupId, String name, boolean isActive, String externalPropertyRef) { ... }
```

Also add a `setIsActive(boolean)` setter (needed for `deactivate`).

### `libs/persistence/src/main/java/persistence/repository/PropertyRepositoryImpl.java`
Implement the updated `create()` and new `deactivate()`.

---

## Task 3: Backend API

### New DTOs — `apps/api/src/main/java/api/application/dto/`

- `PropertyListItemResponse.java` — record with id, name, isActive, externalPropertyRef, createdAt
- `CreatePropertyRequest.java` — record with name, isActive, externalPropertyRef

### New Service — `apps/api/src/main/java/api/application/PropertyManagementService.java`

- `listProperties(UserPrincipal)` — returns only active properties for tenant
- `createProperty(CreatePropertyRequest, UserPrincipal)` — creates and returns DTO
- `deleteProperty(UUID, UserPrincipal)` — soft deletes via deactivate
- All methods require ADMIN role

### New Controller — `apps/api/src/main/java/api/presentation/PropertyManagementController.java`

```
GET    /api/v1/properties        → 200 ApiResponse<List<PropertyListItemResponse>>
POST   /api/v1/properties        → 201 ApiResponse<PropertyListItemResponse>
DELETE /api/v1/properties/{id}   → 204 No Content
```

---

## Task 4: Frontend

- Remove unused nav items, add properties route mapping in AppLayout.tsx
- Add /properties protected route in App.tsx
- Create types.ts, propertiesApi.ts, PropertiesListPage.tsx

---

## Task 5: Tests

- `PropertyManagementServiceTest.java` — Mockito unit tests
- `PropertyManagementControllerTest.java` — @WebMvcTest integration tests
