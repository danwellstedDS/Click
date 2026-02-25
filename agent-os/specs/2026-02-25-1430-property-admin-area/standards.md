# Property Admin Area — Standards

## Backend Standards

### Layering
- **Domain** (`libs/domain`) — interfaces and domain models only; no framework dependencies
- **Persistence** (`libs/persistence`) — JPA entities, repositories, mappers
- **API** (`apps/api`) — Spring Boot controllers, services, DTOs, security

### Patterns
- Services return DTOs, never domain models or entities
- Controllers delegate entirely to services; no business logic in controllers
- All admin endpoints require `ADMIN` role; throw `AuthException("AUTH_403", ..., 403)` for violations
- Use `principal.tenantId()` to scope queries to the current PropertyGroup
- DTOs are Java records in `api.application.dto`
- Controllers use `ApiResponse<T>` wrapper for all responses
- `ResponseEntity.noContent().build()` for 204 responses

### Validation
- `@NotBlank` on required string fields in request records
- Validate in service layer, throw `DomainError.ValidationError` for violations

### Testing
- Service tests: `@ExtendWith(MockitoExtension.class)`, `@InjectMocks`, `@Mock`
- Controller tests: `@WebMvcTest`, `@AutoConfigureMockMvc(addFilters = false)`, `@MockitoBean`
- Spring Boot 4.0.0 package: `org.springframework.boot.webmvc.test.autoconfigure`

## Frontend Standards

### Component Library
- Use only `@derbysoft/neat-design` components
- No raw Ant Design imports

### Feature Structure
```
src/features/{feature}/
  types.ts          — TypeScript interfaces
  {feature}Api.ts   — API calls using apiRequest()
  {Feature}ListPage.tsx — Page component
```

### Patterns
- `apiRequest<T>()` from `../../lib/apiClient` for all API calls
- Loading state: `Spinner` component
- Error state: `toast.error(message)`
- Success state: `toast.success(message)`
- Wrap pages in `<AppLayout title="..." breadcrumb="...">`
- Delete actions require confirmation modal before executing
- Forms use `Form.useForm()` with `Form.Item` validation rules

### Routing
- All admin pages wrapped in `<PrivateRoute><AdminRoute>...</AdminRoute></PrivateRoute>`
- Routes defined in `App.tsx`
- Nav item → route mapping in `NAV_ROUTES` in `AppLayout.tsx`

## Workflow Standards

- Implement backend first, then frontend
- All new files follow existing file and package naming conventions
- No new dependencies — use what is already in the project
