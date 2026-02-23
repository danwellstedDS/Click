# User Management — Shaping Notes

## Problem

Tenant admins have no way to manage users in the system today. They cannot see who has access, invite new users, or remove users who should no longer have access. This creates both a security gap and an operational burden (requires database-level intervention to manage access).

## Appetite

Medium feature — backend CRUD + frontend feature module. No new infrastructure (DB tables exist, auth infrastructure exists). Target: 1 sprint.

## Solution

An admin-only `/users` section in the web app backed by a `/api/v1/users` REST API. The four operations needed are: list users in tenant, create a user (with temp password), view user detail, and delete a user.

### Password on Create

We skip email infrastructure for MVP. Instead:
- Admin provides email + role
- API generates a `UUID.randomUUID()` temp password, BCrypt-hashes it, stores the hash
- API returns the **raw** temp password **once** in the create response
- Admin copies and shares it out-of-band

This is secure (hash stored, raw value shown once) and avoids email setup complexity.

### Role Mapping

Users have a `Role` (ADMIN | VIEWER) per tenant membership. `UserListItemResponse` includes the role for the queried tenant. `UserDetailResponse` includes all memberships.

### Access Control

- **Backend**: `UserManagementService` checks `principal.role() == Role.ADMIN` and throws `AuthException(403)` for non-admins. The `SecurityConfig` permits `/api/v1/users/**` for authenticated users; the role check is enforced at the service layer.
- **Frontend**: `AdminRoute` component wraps routes and redirects non-admin users to `/`.

## Scope

### In scope
- List users in current tenant
- Create user with email + role (temp password returned once)
- View user detail (info + memberships)
- Delete user (removes all memberships + user record)
- Admin-only access guard (FE + BE)
- Nav item in sidebar

### Out of scope (follow-ups)
- Email invitations
- Password reset flow
- Edit user role
- Full audit log
- Pagination / search on user list
- Multi-tenant user transfer

## Key Risks

- **Temp password UX**: Admin must copy the password immediately — there is no "resend". Mitigated by showing it in a modal with a clear copy affordance.
- **Cascade delete**: Deleting a user removes all their tenant memberships first (explicit delete), then the user record. If the user has memberships in other tenants, those are also cleaned up.
- **Self-delete**: The service prevents an admin from deleting themselves to avoid lockout.
