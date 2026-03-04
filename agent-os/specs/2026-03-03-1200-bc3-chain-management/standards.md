# BC3 Chain Management — Standards

## Backend Standards

### Domain layer
- Aggregates are `final` classes with private constructor, static `create()` and `reconstitute()` factories
- `create()` emits domain events; `reconstitute()` does not
- Domain events are Java records in `domain/events/`
- Value objects (enums, records) live in `domain/valueobjects/`
- Domain interfaces (repositories) in `domain/` root
- `DomainError.Conflict` for illegal state transitions; `DomainError.NotFound` for missing entities; `DomainError.Forbidden` for auth failures; `DomainError.ValidationError` for input errors

### Application layer
- Services are `@Service` annotated Spring beans
- Mutating operations are `@Transactional`
- Admin guard uses `requireAdmin(principal)` checking `principal.role() != Role.ADMIN`
- Events published via `InProcessEventBus.publish(EventEnvelope.of(...))`

### Infrastructure layer
- JPA entities in `infrastructure/persistence/entity/`
- Mappers are `final` utility classes with static methods in `infrastructure/persistence/mapper/`
- Repository impls in `infrastructure/persistence/repository/`

### Interfaces layer
- DTOs are Java records in `interfaces/http/dto/`
- Controllers use `@RestController`, `@RequestMapping`, `@AuthenticationPrincipal UserPrincipal`
- All responses wrapped in `ApiResponse<T>` from `com.derbysoft.click.sharedkernel.api.ApiResponse`
- Request ID extracted from `HttpServletRequest.getAttribute("requestId")`

### DB migrations
- Flyway naming: `V{YYYYMMDDNNNN}__{description}.sql`
- Additive-only migrations; no destructive changes to existing tables in production

## Frontend Standards

### Feature modules
- Each feature in `apps/web/src/features/{featureName}/`
- Types in `types.ts`, API client in `{featureName}Api.ts`, main page in `{FeatureName}ListPage.tsx`
- Use `apiRequest<T>` from `@/lib/apiClient` for all API calls

### Component patterns
- Pages wrapped in `<AppLayout title="..." breadcrumb="...">`
- Tables use neat-design `Table` component with `dataSource`, `columns`, `rowKey`
- Status shown with neat-design `Tag` (green for ACTIVE, grey for INACTIVE)
- Modals use neat-design `Modal` with `Form` + `Form.Item` for create/edit
- Toast notifications via neat-design `toast.success()` / `toast.error()`
- Loading state with `useState<boolean>` and neat-design `Spinner`

### Route guards
- All authenticated routes: `<PrivateRoute><...></PrivateRoute>`
- Admin-only routes: `<PrivateRoute><AdminRoute><...></AdminRoute></PrivateRoute>`

### Sidebar
- Nav items in `NAV_ITEMS` array in `AppLayout.tsx`
- Admin-only sections rendered conditionally: `user?.role === 'ADMIN'`
- Submenu groups use ant-design Menu `children` property
