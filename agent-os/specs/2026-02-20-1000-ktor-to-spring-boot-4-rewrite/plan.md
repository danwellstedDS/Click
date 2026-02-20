# Plan: Rewrite Backend — Ktor/Kotlin → Spring Boot 4 / Java 25

## Context

The backend codebase is a complete Ktor + Exposed (Kotlin) implementation of the auth/access control layer (users, tenants, roles, JWT). The team has adopted new standards prescribing Java 25 + Spring Boot 4 + Spring Data JPA. The goal is a full stack rewrite that preserves the existing domain concepts and feature set (login, refresh tokens, tenant switching, `/me`, JWT cookie auth) while replacing every framework and language construct with idiomatic Java 25 / Spring Boot 4 equivalents.

**Current stack:** Kotlin 2.0.21 · Ktor 2.3.12 · Exposed 0.54.0 · JVM 21
**Target stack:** Java 25 · Spring Boot 4 · Spring MVC · Spring Data JPA · Spring Security · JVM 25

**Features preserved (same endpoints, same behaviour):**
- `POST /api/v1/auth/login` — email + password → JWT + refresh token (cookies)
- `POST /api/v1/auth/refresh` — token rotation
- `POST /api/v1/auth/switch-tenant` — switch active tenant (JWT required)
- `GET /api/v1/auth/me` — current user + tenant info (JWT required)
- `POST /api/v1/auth/logout` — clear refresh token
- `GET /health`, `GET /api/health` — health checks

---

## Task 1: Save Spec Documentation ✅

Created `agent-os/specs/2026-02-20-1000-ktor-to-spring-boot-4-rewrite/` containing:
- `plan.md` — this document
- `shape.md` — scope, decisions, context
- `standards.md` — backend/standards, backend/api-spec, documentation/standards, quality/testing
- `references.md` — mapping of all Kotlin source files to their Java equivalents

---

## Task 2: Update Build Configuration ✅

**`gradle/libs.versions.toml`:**
- Removed: `kotlin`, `ktor.*`, `exposed.*`, `bcrypt` versions + libraries + plugins
- Added: `spring-boot = "4.0.0"`, `spring-dependency-management = "1.1.7"`, `java-jwt = "4.4.0"`
- Kept: `flyway`, `postgres`, `logback`, `junit`, `assertj`, `testcontainers`

**`build.gradle.kts` (root):**
- Removed `kotlin.jvm` plugin; declared `spring-boot` and `spring-dependency-management` as `apply false`

**`settings.gradle.kts`:**
- Removed `kotlin.jvm` plugin management

**`libs/domain/build.gradle.kts`:**
- Replaced `kotlin.jvm` plugin with `java`
- Set Java toolchain to 25
- Test deps: `junit-api`, `junit-engine`, `assertj`

**`libs/persistence/build.gradle.kts`:**
- Replaced `kotlin.jvm` + `exposed.*` with Java plugin + `spring-dependency-management`
- Added Spring Boot BOM import
- Deps: `spring-boot-starter-data-jpa`, `flyway-core`, `flyway-postgres`, `postgres`
- Test deps: `spring-boot-starter-test`, `spring-boot-testcontainers`, `testcontainers-junit`, `testcontainers-pg`

**`apps/api/build.gradle.kts`:**
- Replaced `kotlin.jvm` + `application` plugins with `java` + `spring-boot` + `spring-dependency-management`
- Removed all Ktor and bcrypt deps
- Added: `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-actuator`, `java-jwt`

---

## Task 3: Rewrite Domain Layer ✅

Created `libs/domain/src/main/java/domain/`:

| File | Pattern | Key Details |
|---|---|---|
| `User.java` | `final class` | UUID id, static `create()` factory, accessor methods |
| `Tenant.java` | `final class` | UUID id, accessor methods |
| `TenantMembership.java` | `record` | Immutable value object |
| `Role.java` | `enum` | `ADMIN`, `VIEWER` |
| `AuthClaims.java` | `record` | userId, tenantId, email, role |
| `RefreshToken.java` | `final class` | UUID id, static `create()` factory |
| `Health.java` | `record` | `status` field, defaults to `"ok"` |
| `error/DomainError.java` | `sealed abstract class` | `NotFound`, `ValidationError`, `Conflict` inner classes |
| `repository/UserRepository.java` | `interface` | `findByEmail()`, `findById()`, `create()` |
| `repository/TenantMembershipRepository.java` | `interface` | `findByUserId()`, `findByUserAndTenant()`, `create()` |
| `repository/RefreshTokenRepository.java` | `interface` | `create()`, `findByTokenHash()`, `deleteByTokenHash()`, `deleteExpiredForUser()` |

Deleted all `.kt` files from `libs/domain/src/main/kotlin/`.

---

## Task 4: Rewrite Persistence Layer ✅

Created `libs/persistence/src/main/java/persistence/`:

**`entity/` — JPA @Entity classes:**
- `UserEntity.java` — `@Entity @Table(name="users")`, UUID PK with `@GeneratedValue(UUID)`, `@CreationTimestamp`, `@UpdateTimestamp`
- `TenantEntity.java` — same pattern
- `TenantMembershipEntity.java` — plain UUID foreign keys (not `@ManyToOne`), `@Enumerated(STRING)` for role
- `RefreshTokenEntity.java` — UUID PK, `tokenHash` unique

**`repository/` — Spring Data + domain implementations:**
- `UserJpaRepository.java` — `extends JpaRepository<UserEntity, UUID>`, `findByEmail()`
- `TenantMembershipJpaRepository.java` — `findByUserId()`, `findByUserIdAndTenantId()`
- `RefreshTokenJpaRepository.java` — `findByTokenHash()`, `deleteByTokenHash()`, `@Query` for expired cleanup
- `UserRepositoryImpl.java` — `@Repository`, implements `domain.repository.UserRepository`
- `TenantMembershipRepositoryImpl.java` — implements domain interface
- `RefreshTokenRepositoryImpl.java` — implements domain interface

**`mapper/` — Entity ↔ Domain converters:**
- `UserMapper.java`, `TenantMembershipMapper.java`, `RefreshTokenMapper.java` — static `toDomain()` / `toEntity()` methods

Deleted all `.kt` files from `libs/persistence/src/main/kotlin/`.

---

## Task 5: Rewrite API Layer ✅

Created `apps/api/src/main/java/api/`:

**Application:**
- `ApiApplication.java` — `@SpringBootApplication(scanBasePackages={"api","persistence"})`
- `ApiResponse.java` — generic record with nested `ErrorDetail` and `Meta` records, static `success()` / `error()` factories

**Presentation:**
- `presentation/AuthController.java` — `@RestController`, 5 endpoints, cookie handling via `HttpServletResponse`/`HttpServletRequest`
- `presentation/HealthController.java` — `GET /health`, `GET /api/health`
- `presentation/GlobalExceptionHandler.java` — `@ControllerAdvice`, pattern-matching switch on `DomainError`

**Application:**
- `application/AuthService.java` — `@Service`, all auth business logic
- `application/dto/LoginRequest.java` — `record` with `@NotBlank`
- `application/dto/SwitchTenantRequest.java` — `record`

**Security:**
- `security/SecurityConfig.java` — stateless JWT filter chain, CORS, BCryptPasswordEncoder bean
- `security/JwtAuthFilter.java` — reads `auth_token` cookie, validates JWT, sets SecurityContext
- `security/JwtService.java` — Auth0 java-jwt wrapper: `createAccessToken()`, `verifyToken()`, `extractClaims()`
- `security/UserPrincipal.java` — implements `UserDetails`, holds userId/tenantId/email/role
- `security/RequestIdFilter.java` — generates UUID, sets `X-Request-Id` header + request attribute

**Resources:**
- `src/main/resources/application.yml` — datasource, flyway, jpa, jwt config, CORS, actuator

---

## Task 6: Rewrite Tests ✅

**Domain tests** (`libs/domain/src/test/java/domain/`):
- `RoleTest.java` — plain JUnit 5
- `AuthClaimsTest.java` — JUnit 5 + AssertJ

**Persistence tests** (`libs/persistence/src/test/java/persistence/`):
- `TestcontainersConfig.java` — `@TestConfiguration`, static `@Container @ServiceConnection PostgreSQLContainer`
- `UserRepositoryImplTest.java` — `@DataJpaTest` + `@AutoConfigureTestDatabase(replace=NONE)` + `@Import(TestcontainersConfig)`
- `TenantMembershipRepositoryImplTest.java` — same pattern
- `RefreshTokenRepositoryImplTest.java` — same pattern

**Controller tests** (`apps/api/src/test/java/api/presentation/`):
- `AuthControllerTest.java` — `@WebMvcTest(AuthController.class)` + `@MockitoBean AuthService` + `@MockitoBean JwtService`

**Service tests** (`apps/api/src/test/java/api/application/`):
- `AuthServiceTest.java` — `@ExtendWith(MockitoExtension.class)` + `@Mock` repos + `@InjectMocks AuthService`

---

## Task 7: Update Infrastructure ✅

- **`Dockerfile`** — `FROM eclipse-temurin:25-jdk AS builder`, `./gradlew :apps:api:bootJar`, `FROM eclipse-temurin:25-jre`
- **`sonar-project.properties`** — new at project root
- **`gradle/wrapper/gradle-wrapper.properties`** — updated to Gradle 8.12 for Java 25 support
- Deleted all `.kt` files from `apps/` and `libs/` source trees

---

## Verification Checklist

- [ ] `./gradlew build` — compiles with no errors (Java 25 source)
- [ ] `./gradlew test` — all tests pass; JaCoCo ≥ 90% coverage
- [ ] `docker compose up --build` — API starts, Flyway runs, PostgreSQL connects
- [ ] `curl -X POST localhost:8080/api/v1/auth/login -d '{"email":"admin@example.com","password":"password"}' -H 'Content-Type: application/json'` — returns JWT cookie + JSON body
- [ ] `curl localhost:8080/health` — returns `{"status":"ok"}`
- [ ] No `.kt` files in `apps/` or `libs/` source trees

## Testing Requirement

Run the full test suite after the migration (`./gradlew test`) and confirm coverage targets in the verification checklist.
