# References: Ktor → Spring Boot 4 Rewrite

## Source Files Studied (Kotlin → Java Mapping)

### Domain Layer
| Kotlin Source | Java Target | Notes |
|---|---|---|
| `libs/domain/src/main/kotlin/domain/User.kt` | `domain/User.java` | `data class` → `final class` with static `create()` factory |
| `libs/domain/src/main/kotlin/domain/Tenant.kt` | `domain/Tenant.java` | `data class` → `final class` |
| `libs/domain/src/main/kotlin/domain/TenantMembership.kt` | `domain/TenantMembership.java` | `data class` → `record` (immutable value) |
| `libs/domain/src/main/kotlin/domain/Role.kt` | `domain/Role.java` | `enum` → `enum` (unchanged) |
| `libs/domain/src/main/kotlin/domain/AuthClaims.kt` | `domain/AuthClaims.java` | `data class` → `record` |
| `libs/domain/src/main/kotlin/domain/RefreshToken.kt` | `domain/RefreshToken.java` | `data class` → `final class` |
| `libs/domain/src/main/kotlin/domain/Health.kt` | `domain/Health.java` | `data class` → `record` |

### Persistence Layer
| Kotlin Source | Java Target | Notes |
|---|---|---|
| `libs/persistence/src/main/kotlin/persistence/Db.kt` | (deleted) | Replaced by Spring DataSource autoconfiguration + Flyway starter |
| `libs/persistence/src/main/kotlin/persistence/tables/UserTable.kt` | `persistence/entity/UserEntity.java` | Exposed `Table` object → JPA `@Entity` |
| `libs/persistence/src/main/kotlin/persistence/tables/TenantTable.kt` | `persistence/entity/TenantEntity.java` | Same pattern |
| `libs/persistence/src/main/kotlin/persistence/tables/TenantMembershipTable.kt` | `persistence/entity/TenantMembershipEntity.java` | Same pattern |
| `libs/persistence/src/main/kotlin/persistence/tables/RefreshTokenTable.kt` | `persistence/entity/RefreshTokenEntity.java` | Same pattern |
| `libs/persistence/src/main/kotlin/persistence/ports/UserRepositoryPort.kt` | `domain/repository/UserRepository.java` | Moved to domain module |
| `libs/persistence/src/main/kotlin/persistence/ports/TenantMembershipRepositoryPort.kt` | `domain/repository/TenantMembershipRepository.java` | Moved to domain module |
| `libs/persistence/src/main/kotlin/persistence/ports/RefreshTokenRepositoryPort.kt` | `domain/repository/RefreshTokenRepository.java` | Moved to domain module |
| `libs/persistence/src/main/kotlin/persistence/UserRepository.kt` | `persistence/repository/UserRepositoryImpl.java` | Exposed transactions → JPA via Spring Data |
| `libs/persistence/src/main/kotlin/persistence/TenantMembershipRepository.kt` | `persistence/repository/TenantMembershipRepositoryImpl.java` | Same pattern |
| `libs/persistence/src/main/kotlin/persistence/RefreshTokenRepository.kt` | `persistence/repository/RefreshTokenRepositoryImpl.java` | Same pattern |

### API Layer
| Kotlin Source | Java Target | Notes |
|---|---|---|
| `apps/api/src/main/kotlin/api/Main.kt` | `api/ApiApplication.java` | Ktor `embeddedServer` → `@SpringBootApplication` |
| `apps/api/src/main/kotlin/api/routes/AuthRoutes.kt` | `api/presentation/AuthController.java` + `api/application/AuthService.java` | Route functions split into controller (HTTP) + service (logic) |
| `apps/api/src/main/kotlin/api/plugins/JwtConfig.kt` | `api/security/JwtService.java` + `api/security/JwtAuthFilter.java` + `api/security/SecurityConfig.java` | Ktor auth plugin split into Spring Security components |
| `apps/api/src/main/kotlin/api/plugins/RequestId.kt` | `api/security/RequestIdFilter.java` | Ktor plugin → `OncePerRequestFilter` |
| `apps/api/src/main/kotlin/api/AuthPrincipal.kt` | `api/security/UserPrincipal.java` | Ktor `Principal` → Spring Security `UserDetails` |
| `apps/api/src/main/kotlin/api/Response.kt` | `api/ApiResponse.java` | Helper functions → static factory methods on record |

### Test Files
| Kotlin Test | Java Test | Framework Change |
|---|---|---|
| `domain/AuthClaimsTest.kt` | `domain/AuthClaimsTest.java` | `kotlin.test` → JUnit 5 + AssertJ |
| `domain/RoleTest.kt` | `domain/RoleTest.java` | Same |
| `persistence/UserRepositoryTest.kt` | `persistence/UserRepositoryImplTest.java` | Exposed + custom helper → `@DataJpaTest` + Testcontainers |
| `persistence/TenantMembershipRepositoryTest.kt` | `persistence/TenantMembershipRepositoryImplTest.java` | Same |
| `persistence/RefreshTokenRepositoryTest.kt` | `persistence/RefreshTokenRepositoryImplTest.java` | Same |
| `persistence/DbTestHelper.kt` | `persistence/TestcontainersConfig.java` | Static helper → `@TestConfiguration` + `@ServiceConnection` |
| `api/AuthRoutesTest.kt` | `api/presentation/AuthControllerTest.java` | Ktor `testApplication` → `@WebMvcTest` + `MockMvc` |
| `api/JwtConfigTest.kt` | `api/application/AuthServiceTest.java` | Ktor JWT verify → `@ExtendWith(MockitoExtension)` unit tests |

## Key Behavioural Contracts Preserved

### JWT Claims Structure
```
{
  "sub": "<userId UUID>",
  "tenantId": "<tenantId UUID>",
  "email": "<user email>",
  "role": "ADMIN|VIEWER",
  "iss": "click5",
  "aud": "click5-api",
  "iat": <epoch seconds>,
  "exp": <epoch seconds, iat + 8 hours>
}
```

### Cookie Names & Attributes
- `auth_token`: HttpOnly, Secure (production), MaxAge = 8 hours, Path=/
- `refresh_token`: HttpOnly, Secure (production), MaxAge = 7 days, Path=/

### Refresh Token Storage
- Raw token is a UUID string
- Stored in DB as `SHA-256(rawToken)` (hex-encoded)
- Token rotation: delete old hash, insert new on every `/refresh` call

### Timing Attack Prevention
- BCrypt verify always runs even when user is not found (dummy hash)
- Same error message for "user not found" and "wrong password"

### Error Code Mapping
| Code | Meaning | HTTP Status |
|---|---|---|
| `VAL_001` | Validation error (missing/blank fields) | 400 |
| `AUTH_001` | Invalid credentials | 401 |
| `AUTH_002` | Invalid / missing refresh token | 401 |
| `AUTH_003` | Expired refresh token | 401 |
| `AUTH_403` | No tenant membership | 403 |

## External Dependencies Unchanged
- Auth0 `com.auth0:java-jwt` (already used in Ktor version via `ktor-server-auth-jwt`)
- PostgreSQL driver
- Flyway migrations
