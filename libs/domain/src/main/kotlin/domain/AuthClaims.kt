package domain

import java.util.UUID

data class AuthClaims(
    val userId: UUID,
    val tenantId: UUID,
    val email: String,
    val role: Role
)
