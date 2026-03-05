# BC4 Standards Excerpts

## Backend Standards

### Controller Pattern
- All controllers return `ApiResponse<T>` (success wrapper)
- ADMIN guard: check `principal.role() != Role.ADMIN` → throw `DomainError.Forbidden`
- `requestId` extracted from `HttpServletRequest` attribute `"requestId"`
- Inject `HttpServletRequest` as last parameter to pass `requestId` to `ApiResponse.success(data, requestId)`

### Repository Pattern
- Domain interface in `domain/` package (e.g., `OrganizationRepository`)
- JPA entity in `infrastructure/persistence/entity/`
- JPA repository (Spring Data) in `infrastructure/persistence/repository/`
- Impl class in `infrastructure/persistence/repository/` annotated `@Repository`
- Mapper in `infrastructure/persistence/mapper/` — static `toDomain()` method

### Service Pattern
- `@Service` annotation
- `@Transactional` on write methods
- Throws typed `DomainError` subclasses (never raw exceptions)
- Validates inputs before delegating to domain

### Naming Conventions
- Controllers: `{Module}ManagementController` or `{Context}Controller`
- Services: `{Module}ManagementService` or `{Context}CommandHandler`
- DTOs: `{Noun}Request`, `{Noun}Response`, `{Noun}Summary`

## Frontend Standards

### API Client Pattern
- All API calls via `apiRequest<T>(path, init?)` from `../../lib/apiClient`
- Feature-scoped API files: `features/{feature}/{feature}Api.ts`
- Types exported from `features/{feature}/types.ts`

### Context Pattern
- Auth state lives in `AuthContext`
- Context shape: state fields + action functions
- Load on mount via `useEffect`

### Component Pattern
- Page components use `AppLayout` wrapper
- Ant Design components from `@derbysoft/neat-design`
- Icons from `@ant-design/icons`
- `Form.useForm()` for modals, `Form.Item` for fields

## Access Control Model

```
Organization
    └── ScopeAccessGrant (role: GrantRole.ADMIN|VIEWER|...)
            └── AccessScope (type: ScopeType.PROPERTY_GROUP)
                    └── PropertyGroup (chain)

User
    └── TenantMembership (role: Role.ADMIN|VIEWER|...)
            └── PropertyGroup (chain)  ← tenantId
```

`ScopeType` values: `PROPERTY_GROUP`, `PROPERTY`, `PORTFOLIO`
`GrantRole` values: `ADMIN` (and others)
`Role` values: `VIEWER`, `ANALYST`, `MANAGER`, `ADMIN`, `SUPPORT`
