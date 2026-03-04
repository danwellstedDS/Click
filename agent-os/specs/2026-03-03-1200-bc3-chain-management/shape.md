# BC3 Chain Management — Shaping Notes

## Problem

Derbysoft Admins currently have no UI or API to manage PropertyGroups (chains). Creating a chain requires direct DB access. This is a significant operational friction point and a security risk.

## Appetite

Medium batch — 1 sprint. Scope is tightly bounded: CRUD on chains (create, list, get, update status). No bulk operations, no hierarchy management, no property assignment.

## Solution

### Core shape

- Add `status` (ACTIVE | INACTIVE) to `PropertyGroup` — this is the primary new piece of state
- Expose a dedicated `/api/v1/chains` endpoint group, ADMIN-only
- Add a "Chain Management" page in the admin sidebar

### What's out of scope

- Editing chain name, timezone, currency after creation (update endpoint not needed yet)
- Hierarchy management (parent/child chains)
- Property assignment to chains via this UI (that's handled elsewhere)
- Bulk status updates

### Key decisions

1. **Status as value object enum** — `ChainStatus { ACTIVE, INACTIVE }` is simple and sufficient; no need for a richer state machine
2. **Status stored as VARCHAR(20)** — readable in DB, easy to extend
3. **`activate()` / `deactivate()` on aggregate** — enforces business rules (conflict on same-state transition) and emits domain events
4. **Separate service** — `ChainManagementService` keeps chain concerns isolated from property management
5. **Frontend toggle pattern** — "Activate"/"Deactivate" button with confirm modal; same table-level UX as Users page

## Rabbit holes to avoid

- Don't add soft-delete; INACTIVE is sufficient
- Don't add pagination to the list endpoint yet (chains are expected to be O(10-100))
- Don't add search/filter to the frontend list
