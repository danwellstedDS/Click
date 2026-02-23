# Applicable Standards

This spec follows the project standards defined in the following files:

## Frontend Standards
Path: `agent-os/standards/frontend/standards.md`

Key rules applied in this feature:
- All components use `@derbysoft/neat-design` exclusively (no raw HTML, no antd direct imports)
- `toast` from neat-design for success/error notifications
- `Form` + `Form.Item` with `rules` for validation (no manual field state per field)
- `apiRequest<T>` from `lib/apiClient.ts` for all HTTP calls
- Feature-sliced structure: `features/users/{types,usersApi,UsersListPage,UserDetailPage}.ts(x)`
- TypeScript, functional components, hooks only

## Backend Standards
Path: `agent-os/standards/backend/standards.md`

Key rules applied in this feature:
- Layered DDD architecture: Presentation → Application → Domain → Persistence
- Repository interfaces in `libs/domain` (no Spring/JPA annotations)
- JPA implementations in `libs/persistence`
- Constructor injection only (no `@Autowired` field injection)
- `record` types for all DTOs
- `DomainError.NotFound` / `DomainError.ValidationError` / `DomainError.Conflict` for domain errors
- `AuthException` for auth/authz errors (handled by `GlobalExceptionHandler`)
- Manual validation in service layer (consistent with existing `AuthService` pattern)

## API Spec
Path: `agent-os/standards/backend/api-spec.md`

Endpoints follow REST conventions:
```
GET    /api/v1/users       → 200 List<UserListItemResponse>
POST   /api/v1/users       → 201 CreateUserResponse
GET    /api/v1/users/{id}  → 200 UserDetailResponse
DELETE /api/v1/users/{id}  → 204 No Content
```

All success responses wrapped in `ApiResponse<T>` (existing wrapper). Error responses via `GlobalExceptionHandler`.

## Workflow Standards
Path: `agent-os/standards/workflow/standards.md`

Branch naming: `feature/full-user-management`
Commit convention: `feat(full): add user management CRUD`
