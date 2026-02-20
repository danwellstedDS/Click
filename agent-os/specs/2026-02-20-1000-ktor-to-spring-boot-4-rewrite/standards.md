# Standards Applied: Ktor → Spring Boot 4 Rewrite

## backend/standards

### Language & Runtime
- **Java 25** — use records, sealed classes, pattern-matching switch (all GA)
- **Spring Boot 4** — Spring Framework 7, Jakarta EE 11 (`jakarta.*` packages throughout)
- Gradle Kotlin DSL for all build scripts
- Java toolchain `languageVersion = JavaLanguageVersion.of(25)` in every subproject

### Project Structure (DDD Layering)
```
libs/domain/           — pure domain: entities, value objects, repository interfaces, errors
libs/persistence/      — Spring Data JPA: @Entity classes, JpaRepository interfaces, @Repository impls, mappers
apps/api/              — Spring Boot app: @RestController, @Service, Security config, DTOs, filters
```

### Naming Conventions
- Domain classes: no framework annotations, no Spring imports
- JPA entities: suffix `Entity` (e.g., `UserEntity`)
- Spring Data interfaces: suffix `JpaRepository` (e.g., `UserJpaRepository`)
- Domain port implementations: suffix `Impl` (e.g., `UserRepositoryImpl`)
- DTOs: suffix `Request` / `Response` as records
- Error codes: `VAL_` (validation), `AUTH_` (authentication/authorisation), `RES_` (resource)

### Domain Model Rules
- Domain classes in `libs/domain` must not import Spring or Jakarta frameworks
- Repository interfaces (ports) belong in `domain.repository` package
- `DomainError` sealed hierarchy for typed error propagation
- Use `record` for pure value objects (immutable, no identity beyond value)
- Use `final class` for entities with identity (UUID id field)

### Dependency Rules
- `libs/domain` → no external dependencies (pure Java)
- `libs/persistence` → depends on `:libs:domain`, Spring Data JPA, Flyway, PostgreSQL driver
- `apps/api` → depends on `:libs:domain`, `:libs:persistence`, Spring Boot starters

### Error Handling
- Service methods throw `DomainError` subclasses (runtime exceptions)
- `GlobalExceptionHandler @ControllerAdvice` maps `DomainError` to HTTP responses using pattern-matching switch
- Never expose internal error details to clients
- Consistent error response format: `{"success":false,"error":{"code":"...","message":"..."},"meta":{"requestId":"..."}}`

### Security
- Stateless JWT authentication — no server-side sessions
- JWT in `HttpOnly` cookie (`auth_token`) — never in `Authorization` header for browser clients
- BCrypt cost factor ≥ 12 for password hashing
- Always run BCrypt verify even on non-existent users (timing attack prevention)
- Refresh tokens stored as SHA-256 hash (never raw) — token rotation on every use
- `Secure` cookie flag enabled in production (`ENV=production`)

## backend/api-spec

### Endpoint Contract (preserved from Ktor implementation)

#### POST /api/v1/auth/login
Request: `{"email": "string", "password": "string"}`
Response 200: `{"success":true,"data":{"token":"...","refreshToken":"...","user":{"id":"...","email":"..."},"tenants":[{"tenantId":"...","role":"ADMIN|VIEWER"}]},"meta":{"requestId":"..."}}`
Sets cookies: `auth_token` (8h), `refresh_token` (7d)
Errors: 400 VAL_001 (missing fields), 401 AUTH_001 (invalid credentials)

#### POST /api/v1/auth/refresh
Reads: `refresh_token` cookie
Response 200: `{"success":true,"data":{"token":"..."},"meta":{"requestId":"..."}}`
Sets cookies: rotated `auth_token` + `refresh_token`
Errors: 401 AUTH_002 (missing/invalid token), 401 AUTH_003 (expired token)

#### POST /api/v1/auth/switch-tenant
Requires: `auth_token` cookie (JWT)
Request: `{"tenantId": "uuid"}`
Response 200: `{"success":true,"data":{"token":"..."},"meta":{"requestId":"..."}}`
Sets cookie: new `auth_token`
Errors: 400 VAL_001 (invalid tenantId), 403 AUTH_403 (no membership)

#### GET /api/v1/auth/me
Requires: `auth_token` cookie (JWT)
Response 200: `{"success":true,"data":{"id":"...","email":"...","tenantId":"...","role":"..."},"meta":{"requestId":"..."}}`

#### POST /api/v1/auth/logout
Requires: `auth_token` cookie (JWT)
Deletes refresh token from DB, clears both cookies
Response 200: `{"success":true,"data":{"message":"Logged out"},"meta":{"requestId":"..."}}`

#### GET /health, GET /api/health
Response 200: `{"status":"ok"}` (no auth required)

### Response Envelope
```json
// Success
{
  "success": true,
  "data": { ... },
  "meta": { "requestId": "uuid" }
}
// Error
{
  "success": false,
  "error": { "code": "VAL_001", "message": "human readable" },
  "meta": { "requestId": "uuid" }
}
```

### HTTP Status Codes
| Condition | Status |
|---|---|
| Success | 200 OK |
| Validation error | 400 Bad Request |
| Auth failure | 401 Unauthorized |
| Forbidden (no membership) | 403 Forbidden |
| Resource not found | 404 Not Found |
| Conflict (duplicate) | 409 Conflict |

## documentation/standards

### Code Documentation
- Public API methods should have brief Javadoc on non-obvious behaviour
- Security-sensitive code (JWT, BCrypt) must have inline comments explaining security decisions
- No `TODO` comments in production code

### README Updates
- Update stack section to reflect Java 25 / Spring Boot 4
- Keep local dev instructions current (docker compose up)
- Document environment variables

### Architecture Decision Records
- ADRs stored in `agent-os/specs/` as spec documents
- Each significant architectural decision documented in `shape.md`

## quality/testing

### Test Coverage Targets
- JaCoCo line coverage ≥ 90% for `apps/api` and `libs/`
- All public service methods covered by unit tests
- All controller endpoints covered by `@WebMvcTest` slice tests
- All repository operations covered by `@DataJpaTest` + Testcontainers

### Test Naming
- `camelCase` method names (no backtick strings)
- Descriptive: `shouldReturnJwtCookiesWhenLoginSucceeds()`
- No `@DisplayName` annotations (rely on method names)

### Test Structure
- **Domain tests**: Plain JUnit 5, no Spring context, instantaneous
- **Persistence tests**: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace=NONE)` + Testcontainers PostgreSQL
- **Service tests**: `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks`
- **Controller tests**: `@WebMvcTest` + `@MockitoBean` — test HTTP layer only

### CI Requirements
- All tests pass before merge
- No skipped/ignored tests without justification
- SonarQube analysis configured via `sonar-project.properties`
