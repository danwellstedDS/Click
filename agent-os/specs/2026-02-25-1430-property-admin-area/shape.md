# Property Admin Area — Shape

## Problem

The `properties` table and domain model exist but there is no admin UI or API to manage them. Admins cannot create, view, or delete properties through the application.

## Appetite

Small batch — one session to wire up existing domain to new API endpoints and a list page.

## Solution

**Backend:** Three endpoints mirroring the users management API:
- `GET /api/v1/properties` — list active properties for current tenant
- `POST /api/v1/properties` — create a property
- `DELETE /api/v1/properties/{id}` — soft delete (set is_active = false)

**Frontend:** A `PropertiesListPage` that mirrors `UsersListPage` — table with Name, Active status, External Ref, Created At, and Actions (View stub + Delete with confirmation modal).

## Boundaries (Out of Scope)

- Property profile/edit page — "View" navigates to `/properties/:id` but no page exists yet
- Bulk operations
- Property group management
- Any non-admin access to the property list

## Key Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Delete strategy | Soft delete (`is_active = false`) | Preserves data integrity; properties may be referenced elsewhere |
| Active filter | List only shows active properties | Inactive = archived; separate audit view if needed later |
| Tenant scoping | `principal.tenantId()` = PropertyGroup ID | Already established in auth flow |
| Create defaults | `isActive = true` | New properties should be active by default |

## Rabbit Holes Avoided

- Do not implement property update/profile page — scope to list + create + delete only
- Do not add pagination — property counts per tenant are small
- Do not add search/filter — premature for current data volumes
