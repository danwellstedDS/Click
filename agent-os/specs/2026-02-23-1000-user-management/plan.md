# User Management Feature — Implementation Plan

## Context

Admins need to manage users within their tenant — listing existing users, inviting new ones, viewing a user's profile, and removing users. There is no user management UI or API today. The backend already has a `User` domain model, `UserRepository`, and `users` / `tenant_memberships` tables, so the database layer is largely in place. This plan adds the missing CRUD surface end-to-end: new backend endpoints, a frontend feature module, a new nav entry, and an admin-only guard.

---

## Key Decisions

- **Password on create**: admin creates a user with email + role; the API generates a random UUID temp password, hashes it, and returns it once in the response for the admin to share. No email-sending infrastructure required for MVP.
- **Activity section**: shows the user's tenant memberships + account timestamps (created/updated). Full audit log is out of scope.
- **Access control**: ADMIN-only. Frontend: `AdminRoute` component. Backend: role check in service via `UserPrincipal`.
- **No new migrations needed**: `users` and `tenant_memberships` tables already exist.

---

## Files Created / Modified

### Backend — domain layer
| File | Action |
|---|---|
| `libs/domain/src/main/java/domain/repository/UserRepository.java` | Add `findAllByTenantId(UUID tenantId)` and `deleteById(UUID id)` |
| `libs/domain/src/main/java/domain/repository/TenantMembershipRepository.java` | Add `findAllByTenantId(UUID tenantId)` and `deleteByUserId(UUID userId)` |
| `libs/persistence/src/main/java/persistence/repository/UserRepositoryImpl.java` | Implement new methods |
| `libs/persistence/src/main/java/persistence/repository/TenantMembershipRepositoryImpl.java` | Implement new methods |
| `libs/persistence/src/main/java/persistence/repository/TenantMembershipJpaRepository.java` | Add `findAllByTenantId` and `deleteByUserId` |

### Backend — application layer
| File | Action |
|---|---|
| `apps/api/src/main/java/api/application/UserManagementService.java` | NEW — listUsers, createUser, getUser, deleteUser |
| `apps/api/src/main/java/api/application/dto/UserListItemResponse.java` | NEW — id, email, role, createdAt |
| `apps/api/src/main/java/api/application/dto/UserDetailResponse.java` | NEW — id, email, role, createdAt, updatedAt, memberships |
| `apps/api/src/main/java/api/application/dto/CreateUserRequest.java` | NEW — email, role |
| `apps/api/src/main/java/api/application/dto/CreateUserResponse.java` | NEW — user info + temporaryPassword |

### Backend — presentation layer
| File | Action |
|---|---|
| `apps/api/src/main/java/api/presentation/UserManagementController.java` | NEW — REST controller at `/api/v1/users` |

### Frontend
| File | Action |
|---|---|
| `apps/web/src/features/auth/AdminRoute.tsx` | NEW — wraps PrivateRoute, redirects non-admins |
| `apps/web/src/features/users/types.ts` | NEW — User, CreateUserRequest, CreateUserResponse types |
| `apps/web/src/features/users/usersApi.ts` | NEW — listUsers, createUser, getUser, deleteUser |
| `apps/web/src/features/users/UsersListPage.tsx` | NEW — table + add modal + delete confirmation |
| `apps/web/src/features/users/UserDetailPage.tsx` | NEW — user profile + memberships/activity |
| `apps/web/src/App.tsx` | Add `/users` and `/users/:id` routes |
| `apps/web/src/components/AppLayout.tsx` | Add "Users" nav item with `TeamOutlined` icon |

---

## Verification

1. `./gradlew build` — no compile errors
2. `POST /api/v1/users` as ADMIN → returns 201 with user + temporaryPassword
3. `GET /api/v1/users` as ADMIN → returns list of users in tenant
4. `GET /api/v1/users/{id}` → returns user detail
5. `DELETE /api/v1/users/{id}` → returns 204, user removed
6. `GET /api/v1/users` as VIEWER → returns 403
7. `npm run dev` — no TS errors; `/users` renders the list page; `/users/:id` renders the detail page
8. Add user flow: opens modal, submits, shows temp password modal, user appears in table
9. Delete flow: confirmation modal, user removed from table
10. Non-admin user navigating to `/users` is redirected to `/`
