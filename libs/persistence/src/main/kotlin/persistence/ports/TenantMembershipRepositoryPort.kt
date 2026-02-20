package persistence.ports

import domain.Role
import domain.TenantMembership
import java.util.UUID

interface TenantMembershipRepositoryPort {
    fun findByUserId(userId: UUID): List<TenantMembership>
    fun findByUserAndTenant(userId: UUID, tenantId: UUID): TenantMembership?
    fun create(userId: UUID, tenantId: UUID, role: Role): TenantMembership
}
