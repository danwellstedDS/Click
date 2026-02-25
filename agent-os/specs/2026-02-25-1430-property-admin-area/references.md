# Property Admin Area — References

## Reference Implementation: Users Feature

The Users feature is the direct reference for this implementation. Every layer of the property admin mirrors the users admin.

### Backend

| Property File | Users Equivalent |
|---|---|
| `PropertyManagementController.java` | `UserManagementController.java` |
| `PropertyManagementService.java` | `UserManagementService.java` |
| `PropertyListItemResponse.java` | `UserListItemResponse.java` |
| `CreatePropertyRequest.java` | `CreateUserRequest.java` |
| `PropertyManagementServiceTest.java` | `AuthServiceTest.java` (pattern) |
| `PropertyManagementControllerTest.java` | `AuthControllerTest.java` (pattern) |

**Key patterns to copy:**
- `requireAdmin(principal)` guard at the top of every service method
- `ApiResponse.success(data, requestId(request))` in controller responses
- `ResponseEntity.status(HttpStatus.CREATED).body(...)` for POST
- `ResponseEntity.noContent().build()` for DELETE
- `@MockitoBean` (not `@MockBean`) in controller tests — Spring Boot 4.0.0
- `@AutoConfigureMockMvc(addFilters = false)` to bypass security filter in tests

### Frontend

| Property File | Users Equivalent |
|---|---|
| `PropertiesListPage.tsx` | `UsersListPage.tsx` |
| `propertiesApi.ts` | `usersApi.ts` |
| `types.ts` | `types.ts` (users) |

**Key patterns to copy:**
- `useState` + `useEffect` for data loading
- `async function loadProperties()` called in useEffect and after mutations
- Modal state pattern: `{ open: boolean, item: T | null }`
- `Form.useForm()` with `onFinish` handler
- `toast.error` / `toast.success` for feedback
- `<AppLayout title="..." breadcrumb="...">` wrapper

### Domain/Persistence

| Property File | Users Equivalent |
|---|---|
| `PropertyRepository.java` | `UserRepository.java` |
| `PropertyEntity.java` | `UserEntity.java` |
| `PropertyRepositoryImpl.java` | `UserRepositoryImpl.java` |
| `PropertyMapper.java` | `UserMapper.java` |

## File Locations

```
# Backend
apps/api/src/main/java/api/presentation/UserManagementController.java
apps/api/src/main/java/api/application/UserManagementService.java
apps/api/src/main/java/api/application/dto/UserListItemResponse.java
apps/api/src/main/java/api/application/dto/CreateUserRequest.java
apps/api/src/main/java/api/ApiResponse.java
apps/api/src/main/java/api/application/AuthException.java
apps/api/src/main/java/api/security/UserPrincipal.java

# Frontend
apps/web/src/features/users/UsersListPage.tsx
apps/web/src/features/users/usersApi.ts
apps/web/src/features/users/types.ts
apps/web/src/lib/apiClient.ts
apps/web/src/components/AppLayout.tsx
apps/web/src/App.tsx

# Tests
apps/api/src/test/java/api/application/AuthServiceTest.java
apps/api/src/test/java/api/presentation/AuthControllerTest.java
apps/api/src/test/java/api/presentation/TestWebConfig.java
```
