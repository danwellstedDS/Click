# Shape: Ktor/Kotlin → Spring Boot 4 / Java 25 Rewrite

## What We're Building

A full backend rewrite that replaces the Ktor + Exposed (Kotlin) auth/access-control layer with an idiomatic Java 25 / Spring Boot 4 / Spring Data JPA / Spring Security stack — preserving all existing domain concepts, API contracts, and feature behaviour.

## Scope

**In scope:**
- All Kotlin source files in `apps/` and `libs/` converted to Java 25
- Build system (Gradle) updated to Spring Boot 4 conventions
- Domain model preserved (User, Tenant, TenantMembership, Role, AuthClaims, RefreshToken)
- All 5 auth endpoints preserved with identical request/response contracts
- Health endpoints preserved
- JWT cookie transport preserved (same cookie names: `auth_token`, `refresh_token`)
- Tests rewritten in JUnit 5 + Mockito + AssertJ
- Docker image updated to `eclipse-temurin:25`

**Out of scope:**
- Flyway SQL migrations (unchanged)
- `docker-compose.yml` (minimal env var updates only)
- Database schema changes
- New features / new endpoints
- Frontend code

## Key Decisions

### Language & Framework
- **Java 25** with sealed classes, records, pattern-matching switch (all GA in Java 21+)
- **Spring Boot 4** (Spring Framework 7, Jakarta EE 11)
- **Spring MVC** (blocking, not reactive) — matches existing synchronous Exposed/Ktor pattern
- **Spring Data JPA + Hibernate** — replaces Exposed ORM
- **Spring Security** — replaces Ktor's JWT auth plugin

### Architecture
- Multi-module Gradle project preserved (`:libs:domain`, `:libs:persistence`, `:apps:api`)
- DDD-style layering: domain ports in `libs/domain`, impls in `libs/persistence`
- Repository ports moved from `persistence/ports/` to `domain/repository/` (domain owns the interface)
- `@SpringBootApplication(scanBasePackages = {"api", "persistence"})` to wire cross-module beans

### Security
- JWT verified in `JwtAuthFilter extends OncePerRequestFilter`
- Auth0 `com.auth0:java-jwt` library (already used in Ktor version)
- `BCryptPasswordEncoder` from Spring Security (replaces `at.favre.lib:bcrypt`)
- Stateless session — `SessionCreationPolicy.STATELESS`
- `HttpOnly` cookies for JWT transport (same names as Ktor version)

### Error Handling
- `DomainError sealed class` hierarchy: `NotFound`, `ValidationError`, `Conflict`
- `GlobalExceptionHandler @ControllerAdvice` with pattern-matching switch
- Error codes preserved: `VAL_001`, `AUTH_001`, `AUTH_002`, `AUTH_003`, `AUTH_403`

### Testing
- `@DataJpaTest` + `@AutoConfigureTestDatabase(replace=NONE)` + Testcontainers for persistence
- `@WebMvcTest` + `@MockitoBean` for controller slice tests
- `@ExtendWith(MockitoExtension.class)` for service unit tests
- Plain JUnit 5 for domain unit tests

## Context / Drivers

- Team adopted new standards: Java 25 + Spring Boot 4 for all backend services
- Existing Ktor codebase was the first implementation of the auth system (auth-system spec `2026-02-20`)
- The API surface (endpoint paths, request/response shapes, cookie names) must remain stable so the frontend is unaffected
- BCrypt cost factor 12 preserved — existing password hashes remain valid
- JWT claims structure preserved (`sub` = userId, `tenantId`, `email`, `role`)

## Constraints

- No changes to SQL migrations
- `JWT_SECRET` env var name unchanged
- Cookie names unchanged: `auth_token` (access), `refresh_token` (refresh)
- `ENV=production` still controls `Secure` flag on cookies
- Flyway migration location: `filesystem:infra/db/migrations` (same path)
