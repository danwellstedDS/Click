package api.plugins

import api.AuthPrincipal
import api.respondWithError
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import domain.AuthClaims
import domain.Role
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.Date
import java.util.UUID

private const val ACCESS_TOKEN_HOURS = 8L
private const val JWT_ISSUER = "click5"
private const val JWT_AUDIENCE = "click5-api"

fun Application.configureJwt(jwtSecret: String) {
    authentication {
        jwt("jwt-auth") {
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JWT_ISSUER)
                    .withAudience(JWT_AUDIENCE)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.subject?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                val tenantId = credential.payload.getClaim("tenantId").asString()?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                val email = credential.payload.getClaim("email").asString()
                val roleStr = credential.payload.getClaim("role").asString()
                val role = roleStr?.let { runCatching { Role.valueOf(it) }.getOrNull() }

                if (userId != null && tenantId != null && email != null && role != null) {
                    AuthPrincipal(AuthClaims(userId, tenantId, email, role))
                } else null
            }
            challenge { _, _ ->
                call.respondWithError(401, "AUTH_001", "Missing or invalid token")
            }
        }
    }
}

fun makeAccessToken(claims: AuthClaims, jwtSecret: String): String {
    val now = System.currentTimeMillis()
    return JWT.create()
        .withIssuer(JWT_ISSUER)
        .withAudience(JWT_AUDIENCE)
        .withSubject(claims.userId.toString())
        .withClaim("tenantId", claims.tenantId.toString())
        .withClaim("email", claims.email)
        .withClaim("role", claims.role.name)
        .withIssuedAt(Date(now))
        .withExpiresAt(Date(now + ACCESS_TOKEN_HOURS * 3600 * 1000))
        .sign(Algorithm.HMAC256(jwtSecret))
}

fun verifierForSecret(jwtSecret: String) = JWT.require(Algorithm.HMAC256(jwtSecret))
    .withIssuer(JWT_ISSUER)
    .withAudience(JWT_AUDIENCE)
    .build()
