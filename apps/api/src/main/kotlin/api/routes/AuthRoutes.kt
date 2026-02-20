package api.routes

import api.AuthPrincipal
import api.respondSuccess
import api.respondWithError
import api.plugins.makeAccessToken
import at.favre.lib.crypto.bcrypt.BCrypt
import domain.AuthClaims
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import persistence.ports.RefreshTokenRepositoryPort
import persistence.ports.TenantMembershipRepositoryPort
import persistence.ports.UserRepositoryPort
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

private const val REFRESH_TOKEN_DAYS = 7L
private const val BCRYPT_COST = 12

data class LoginRequest(val email: String? = null, val password: String? = null)
data class SwitchTenantRequest(val tenantId: String? = null)

data class TenantInfo(val tenantId: String, val role: String)
data class UserInfo(val id: String, val email: String)
data class LoginResponseData(
    val token: String,
    val refreshToken: String,
    val user: UserInfo,
    val tenants: List<TenantInfo>
)
data class MeResponseData(val id: String, val email: String, val tenantId: String, val role: String)
data class TokenResponseData(val token: String)

fun Route.authRoutes(
    userRepo: UserRepositoryPort,
    membershipRepo: TenantMembershipRepositoryPort,
    refreshTokenRepo: RefreshTokenRepositoryPort,
    jwtSecret: String,
    isProduction: Boolean
) {
    route("/api/v1/auth") {

        post("/login") {
            val body = runCatching { call.receive<LoginRequest>() }.getOrDefault(LoginRequest())

            if (body.email.isNullOrBlank() || body.password.isNullOrBlank()) {
                call.respondWithError(400, "VAL_001", "email and password are required")
                return@post
            }

            val user = userRepo.findByEmail(body.email)
            // Always run bcrypt verify to prevent timing attacks
            val dummyHash = "\$2a\$12\$dummyhashfortimingtattackprevention000000000000000000000"
            val hashToVerify = user?.passwordHash ?: dummyHash
            val passwordValid = BCrypt.verifyer().verify(body.password.toCharArray(), hashToVerify).verified

            if (user == null || !passwordValid) {
                call.respondWithError(401, "AUTH_001", "Invalid email or password")
                return@post
            }

            val memberships = membershipRepo.findByUserId(user.id)
            if (memberships.isEmpty()) {
                call.respondWithError(401, "AUTH_001", "No tenant memberships found")
                return@post
            }

            val firstMembership = memberships.first()
            val claims = AuthClaims(user.id, firstMembership.tenantId, user.email, firstMembership.role)
            val accessToken = makeAccessToken(claims, jwtSecret)

            // Refresh tokens use SHA-256 for DB lookup (bcrypt is for passwords)
            val rawRefreshToken = UUID.randomUUID().toString()
            val refreshTokenHash = sha256(rawRefreshToken)
            val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400)
            refreshTokenRepo.create(user.id, refreshTokenHash, expiresAt)

            setAuthCookies(call, accessToken, rawRefreshToken, isProduction)

            call.respondSuccess(
                LoginResponseData(
                    token = accessToken,
                    refreshToken = rawRefreshToken,
                    user = UserInfo(user.id.toString(), user.email),
                    tenants = memberships.map { TenantInfo(it.tenantId.toString(), it.role.name) }
                )
            )
        }

        post("/refresh") {
            val rawToken = call.request.cookies["refresh_token"]
                ?: run {
                    call.respondWithError(401, "AUTH_002", "Missing refresh token")
                    return@post
                }

            val tokenHash = sha256(rawToken)
            val storedToken = refreshTokenRepo.findByTokenHash(tokenHash)
                ?: run {
                    call.respondWithError(401, "AUTH_002", "Invalid refresh token")
                    return@post
                }

            if (storedToken.expiresAt.isBefore(Instant.now())) {
                refreshTokenRepo.deleteByTokenHash(tokenHash)
                call.respondWithError(401, "AUTH_003", "Refresh token expired")
                return@post
            }

            val userRecord = userRepo.findById(storedToken.userId)
                ?: run {
                    call.respondWithError(401, "AUTH_002", "User not found")
                    return@post
                }

            val memberships = membershipRepo.findByUserId(userRecord.id)
            if (memberships.isEmpty()) {
                call.respondWithError(401, "AUTH_002", "No tenant memberships")
                return@post
            }

            val firstMembership = memberships.first()
            val claims = AuthClaims(userRecord.id, firstMembership.tenantId, userRecord.email, firstMembership.role)
            val newAccessToken = makeAccessToken(claims, jwtSecret)

            // Rotate refresh token
            refreshTokenRepo.deleteByTokenHash(tokenHash)
            val newRawRefreshToken = UUID.randomUUID().toString()
            val newTokenHash = sha256(newRawRefreshToken)
            val newExpiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_DAYS * 86400)
            refreshTokenRepo.create(userRecord.id, newTokenHash, newExpiresAt)

            setAuthCookies(call, newAccessToken, newRawRefreshToken, isProduction)

            call.respondSuccess(TokenResponseData(token = newAccessToken))
        }

        authenticate("jwt-auth") {

            post("/switch-tenant") {
                val principal = call.principal<AuthPrincipal>()!!
                val body = runCatching { call.receive<SwitchTenantRequest>() }.getOrDefault(SwitchTenantRequest())

                val targetTenantId = body.tenantId?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                    ?: run {
                        call.respondWithError(400, "VAL_001", "tenantId is required")
                        return@post
                    }

                val membership = membershipRepo.findByUserAndTenant(principal.claims.userId, targetTenantId)
                    ?: run {
                        call.respondWithError(403, "AUTH_403", "No membership for requested tenant")
                        return@post
                    }

                val newClaims = AuthClaims(
                    userId = principal.claims.userId,
                    tenantId = targetTenantId,
                    email = principal.claims.email,
                    role = membership.role
                )
                val newToken = makeAccessToken(newClaims, jwtSecret)

                call.response.cookies.append(
                    name = "auth_token",
                    value = newToken,
                    httpOnly = true,
                    secure = isProduction,
                    maxAge = 8 * 3600,
                    path = "/"
                )

                call.respondSuccess(TokenResponseData(token = newToken))
            }

            get("/me") {
                val claims = call.principal<AuthPrincipal>()!!.claims
                call.respondSuccess(
                    MeResponseData(
                        id = claims.userId.toString(),
                        email = claims.email,
                        tenantId = claims.tenantId.toString(),
                        role = claims.role.name
                    )
                )
            }

            post("/logout") {
                val rawToken = call.request.cookies["refresh_token"]
                if (rawToken != null) {
                    refreshTokenRepo.deleteByTokenHash(sha256(rawToken))
                }
                clearAuthCookies(call, isProduction)
                call.respondSuccess(mapOf("message" to "Logged out"))
            }
        }
    }
}

private fun setAuthCookies(call: ApplicationCall, accessToken: String, refreshToken: String, isProduction: Boolean) {
    call.response.cookies.append(
        name = "auth_token",
        value = accessToken,
        httpOnly = true,
        secure = isProduction,
        maxAge = 8 * 3600,
        path = "/"
    )
    call.response.cookies.append(
        name = "refresh_token",
        value = refreshToken,
        httpOnly = true,
        secure = isProduction,
        maxAge = REFRESH_TOKEN_DAYS.toInt() * 86400,
        path = "/"
    )
}

private fun clearAuthCookies(call: ApplicationCall, isProduction: Boolean) {
    call.response.cookies.append(
        name = "auth_token",
        value = "",
        httpOnly = true,
        secure = isProduction,
        maxAge = 0,
        path = "/"
    )
    call.response.cookies.append(
        name = "refresh_token",
        value = "",
        httpOnly = true,
        secure = isProduction,
        maxAge = 0,
        path = "/"
    )
}

private fun sha256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
}
