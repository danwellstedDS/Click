package domain

import java.time.Instant
import java.util.UUID

data class TenantMembership(
    val id: UUID,
    val userId: UUID,
    val tenantId: UUID,
    val role: Role,
    val createdAt: Instant,
    val updatedAt: Instant
)
