package api

import api.plugins.makeAccessToken
import api.plugins.verifierForSecret
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import domain.AuthClaims
import domain.Role
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JwtConfigTest {

    private val secret = "test-secret-at-least-32-characters-long"
    private val claims = AuthClaims(
        userId = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        email = "test@example.com",
        role = Role.ADMIN
    )

    @Test
    fun `makeToken produces a verifiable token`() {
        val token = makeAccessToken(claims, secret)
        assertNotNull(token)
        val decoded = verifierForSecret(secret).verify(token)
        assertEquals(claims.userId.toString(), decoded.subject)
        assertEquals(claims.tenantId.toString(), decoded.getClaim("tenantId").asString())
        assertEquals(claims.email, decoded.getClaim("email").asString())
        assertEquals(claims.role.name, decoded.getClaim("role").asString())
    }

    @Test
    fun `tampered token is rejected`() {
        val token = makeAccessToken(claims, secret)
        val parts = token.split(".")
        val tampered = "${parts[0]}.${parts[1]}.invalidsignature"
        assertThrows<JWTVerificationException> {
            verifierForSecret(secret).verify(tampered)
        }
    }

    @Test
    fun `token signed with wrong secret is rejected`() {
        val token = makeAccessToken(claims, "wrong-secret-that-is-at-least-32-chars-long")
        assertThrows<SignatureVerificationException> {
            verifierForSecret(secret).verify(token)
        }
    }
}
