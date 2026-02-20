package domain

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class AuthClaimsTest {

    @Test
    fun `holds all fields`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val email = "test@example.com"
        val role = Role.ADMIN

        val claims = AuthClaims(userId, tenantId, email, role)

        assertEquals(userId, claims.userId)
        assertEquals(tenantId, claims.tenantId)
        assertEquals(email, claims.email)
        assertEquals(role, claims.role)
    }

    @Test
    fun `equality is structural`() {
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val a = AuthClaims(userId, tenantId, "a@b.com", Role.VIEWER)
        val b = AuthClaims(userId, tenantId, "a@b.com", Role.VIEWER)
        assertEquals(a, b)
    }
}
