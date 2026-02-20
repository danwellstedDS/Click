package api

import api.plugins.RequestIdPlugin
import api.plugins.configureJwt
import api.plugins.makeAccessToken
import api.routes.authRoutes
import at.favre.lib.crypto.bcrypt.BCrypt
import domain.AuthClaims
import domain.RefreshToken
import domain.Role
import domain.TenantMembership
import domain.User
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import persistence.ports.RefreshTokenRepositoryPort
import persistence.ports.TenantMembershipRepositoryPort
import persistence.ports.UserRepositoryPort
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import kotlin.test.assertContains
import kotlin.test.assertEquals

private const val JWT_SECRET = "test-secret-that-is-at-least-32-characters"
private const val BCRYPT_COST = 12

class AuthRoutesTest {

    // ---- In-memory fakes ----

    class FakeUserRepo : UserRepositoryPort {
        val users = mutableMapOf<String, User>()

        override fun findByEmail(email: String) = users[email]
        override fun findById(id: UUID) = users.values.find { it.id == id }
        override fun create(email: String, passwordHash: String): User {
            val u = User(UUID.randomUUID(), email, passwordHash, Instant.now(), Instant.now())
            users[email] = u
            return u
        }
    }

    class FakeMembershipRepo : TenantMembershipRepositoryPort {
        val memberships = mutableListOf<TenantMembership>()

        override fun findByUserId(userId: UUID) = memberships.filter { it.userId == userId }
        override fun findByUserAndTenant(userId: UUID, tenantId: UUID) =
            memberships.find { it.userId == userId && it.tenantId == tenantId }

        override fun create(userId: UUID, tenantId: UUID, role: Role): TenantMembership {
            val m = TenantMembership(UUID.randomUUID(), userId, tenantId, role, Instant.now(), Instant.now())
            memberships += m
            return m
        }
    }

    class FakeRefreshTokenRepo : RefreshTokenRepositoryPort {
        val tokens = mutableMapOf<String, RefreshToken>()

        override fun create(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken {
            val t = RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, Instant.now())
            tokens[tokenHash] = t
            return t
        }

        override fun findByTokenHash(tokenHash: String) = tokens[tokenHash]

        override fun deleteByTokenHash(tokenHash: String) {
            tokens.remove(tokenHash)
        }

        override fun deleteExpiredForUser(userId: UUID) {
            val now = Instant.now()
            tokens.entries.removeAll { it.value.userId == userId && it.value.expiresAt.isBefore(now) }
        }
    }

    // ---- Helpers ----

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun setupApp(
        userRepo: FakeUserRepo,
        membershipRepo: FakeMembershipRepo,
        refreshTokenRepo: FakeRefreshTokenRepo,
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        application {
            install(RequestIdPlugin)
            install(ContentNegotiation) { jackson() }
            configureJwt(JWT_SECRET)
            routing {
                authRoutes(userRepo, membershipRepo, refreshTokenRepo, JWT_SECRET, isProduction = false)
            }
        }
        block()
    }

    private fun seedUser(
        userRepo: FakeUserRepo,
        membershipRepo: FakeMembershipRepo,
        email: String = "user@example.com",
        password: String = "password123",
        role: Role = Role.ADMIN
    ): Pair<User, UUID> {
        val hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
        val user = userRepo.create(email, hash)
        val tenantId = UUID.randomUUID()
        membershipRepo.create(user.id, tenantId, role)
        return user to tenantId
    }

    // ---- POST /login ----

    @Test
    fun `POST login returns 200 with token and tenants`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()
        val (_, _) = seedUser(userRepo, membershipRepo)

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"user@example.com","password":"password123"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertContains(body, "\"success\":true")
            assertContains(body, "token")
            assertContains(body, "tenants")
        }
    }

    @Test
    fun `POST login returns 401 on wrong password`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()
        seedUser(userRepo, membershipRepo)

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"user@example.com","password":"wrongpassword"}""")
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertContains(response.bodyAsText(), "AUTH_001")
        }
    }

    @Test
    fun `POST login returns 400 on missing fields`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"user@example.com"}""")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertContains(response.bodyAsText(), "VAL_001")
        }
    }

    // ---- POST /refresh ----

    @Test
    fun `POST refresh rotates token and returns 200`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()
        val (user, _) = seedUser(userRepo, membershipRepo)

        val rawToken = UUID.randomUUID().toString()
        val tokenHash = sha256(rawToken)
        refreshTokenRepo.create(user.id, tokenHash, Instant.now().plusSeconds(3600))

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/refresh") {
                header("Cookie", "refresh_token=$rawToken")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertContains(response.bodyAsText(), "token")
        }
    }

    @Test
    fun `POST refresh returns 401 on invalid token`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/refresh") {
                header("Cookie", "refresh_token=invalidtoken")
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertContains(response.bodyAsText(), "AUTH_002")
        }
    }

    // ---- GET /me ----

    @Test
    fun `GET me returns 200 with valid JWT`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()
        val (user, tenantId) = seedUser(userRepo, membershipRepo)

        val token = makeAccessToken(
            AuthClaims(user.id, tenantId, user.email, Role.ADMIN),
            JWT_SECRET
        )

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.get("/api/v1/auth/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertContains(response.bodyAsText(), "email")
        }
    }

    @Test
    fun `GET me returns 401 without token`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.get("/api/v1/auth/me")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    // ---- POST /switch-tenant ----

    @Test
    fun `POST switch-tenant returns 200 with new JWT`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()
        val (user, tenantId) = seedUser(userRepo, membershipRepo)
        val secondTenantId = UUID.randomUUID()
        membershipRepo.create(user.id, secondTenantId, Role.VIEWER)

        val token = makeAccessToken(AuthClaims(user.id, tenantId, user.email, Role.ADMIN), JWT_SECRET)

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/switch-tenant") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"tenantId":"$secondTenantId"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertContains(response.bodyAsText(), "token")
        }
    }

    @Test
    fun `POST switch-tenant returns 403 when no membership`() {
        val userRepo = FakeUserRepo()
        val membershipRepo = FakeMembershipRepo()
        val refreshTokenRepo = FakeRefreshTokenRepo()
        val (user, tenantId) = seedUser(userRepo, membershipRepo)
        val token = makeAccessToken(AuthClaims(user.id, tenantId, user.email, Role.ADMIN), JWT_SECRET)

        setupApp(userRepo, membershipRepo, refreshTokenRepo) {
            val response = client.post("/api/v1/auth/switch-tenant") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"tenantId":"${UUID.randomUUID()}"}""")
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertContains(response.bodyAsText(), "AUTH_403")
        }
    }
}
