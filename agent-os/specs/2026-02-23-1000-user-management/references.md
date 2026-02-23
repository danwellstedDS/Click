# Structural References

## Auth Feature — Primary Reference

The auth feature (`features/auth/`) is the structural reference for this user management feature. Follow the same patterns for:

### Backend

**AuthController** (`apps/api/src/main/java/api/presentation/AuthController.java`)
- Pattern: `@RestController`, `@RequestMapping`, constructor injection, `@AuthenticationPrincipal UserPrincipal`
- Uses `ApiResponse.success(data, requestId(request))` wrapper
- `requestId()` helper extracted as private static method

**AuthService** (`apps/api/src/main/java/api/application/AuthService.java`)
- Pattern: `@Service`, constructor injection, manual input validation
- Throws `DomainError.ValidationError` for business validation failures
- Throws `AuthException` for auth/authz failures (401/403)
- Uses `passwordEncoder.encode()` / `passwordEncoder.matches()`

**AuthException** (`apps/api/src/main/java/api/application/AuthException.java`)
- Reuse this for 403 "Admin access required" errors

**GlobalExceptionHandler** (`apps/api/src/main/java/api/presentation/GlobalExceptionHandler.java`)
- Handles `DomainError` subtypes → appropriate HTTP status
- Handles `AuthException` → HTTP status from exception
- No changes needed — existing handler covers all new error types

**SecurityConfig** (`apps/api/src/main/java/api/security/SecurityConfig.java`)
- `.anyRequest().authenticated()` already covers `/api/v1/users/**`
- No changes needed

**UserPrincipal** (`apps/api/src/main/java/api/security/UserPrincipal.java`)
- Provides `userId()`, `tenantId()`, `role()` used in service auth checks

### Frontend

**PrivateRoute** (`apps/web/src/features/auth/PrivateRoute.tsx`)
- Pattern for `AdminRoute`: same structure, add role check

**authApi.ts** (`apps/web/src/features/auth/authApi.ts`)
- Pattern for `usersApi.ts`: use `apiRequest<T>` from `lib/apiClient`

**LoginPage** (`apps/web/src/features/auth/LoginPage.tsx`)
- Pattern for form modals: `Form`, `Form.Item`, `Button` with `loading` prop

**types.ts** (`apps/web/src/features/auth/types.ts`)
- Pattern for `features/users/types.ts`: plain TypeScript interfaces

## Repository Pattern Reference

**UserRepositoryImpl** (`libs/persistence/src/main/java/persistence/repository/UserRepositoryImpl.java`)
- Delegates to JPA repository, maps via `UserMapper::toDomain`

**TenantMembershipRepositoryImpl** (`libs/persistence/src/main/java/persistence/repository/TenantMembershipRepositoryImpl.java`)
- Same delegation pattern, `TenantMembershipMapper::toDomain`

## Domain Models

**User** (`libs/domain/src/main/java/domain/User.java`)
- Fields: `id`, `email`, `passwordHash`, `createdAt`, `updatedAt`
- Immutable, static factory `User.create(...)`

**TenantMembership** (`libs/domain/src/main/java/domain/TenantMembership.java`)
- Record: `id`, `userId`, `tenantId`, `role`, `createdAt`, `updatedAt`

**Role** (`libs/domain/src/main/java/domain/Role.java`)
- Enum: `ADMIN`, `VIEWER`
