package persistence

import domain.Role
import domain.TenantMembership
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.ports.TenantMembershipRepositoryPort
import persistence.tables.TenantMembershipTable
import java.time.OffsetDateTime
import java.util.UUID

class TenantMembershipRepository : TenantMembershipRepositoryPort {

    override fun findByUserId(userId: UUID): List<TenantMembership> = transaction {
        TenantMembershipTable.select { TenantMembershipTable.userId eq userId }
            .map { row ->
                TenantMembership(
                    id = row[TenantMembershipTable.id],
                    userId = row[TenantMembershipTable.userId],
                    tenantId = row[TenantMembershipTable.tenantId],
                    role = Role.valueOf(row[TenantMembershipTable.role]),
                    createdAt = row[TenantMembershipTable.createdAt].toInstant(),
                    updatedAt = row[TenantMembershipTable.updatedAt].toInstant()
                )
            }
    }

    override fun findByUserAndTenant(userId: UUID, tenantId: UUID): TenantMembership? = transaction {
        TenantMembershipTable.select {
            (TenantMembershipTable.userId eq userId) and (TenantMembershipTable.tenantId eq tenantId)
        }.singleOrNull()?.let { row ->
            TenantMembership(
                id = row[TenantMembershipTable.id],
                userId = row[TenantMembershipTable.userId],
                tenantId = row[TenantMembershipTable.tenantId],
                role = Role.valueOf(row[TenantMembershipTable.role]),
                createdAt = row[TenantMembershipTable.createdAt].toInstant(),
                updatedAt = row[TenantMembershipTable.updatedAt].toInstant()
            )
        }
    }

    override fun create(userId: UUID, tenantId: UUID, role: Role): TenantMembership = transaction {
        val now = OffsetDateTime.now()
        val id = TenantMembershipTable.insert {
            it[TenantMembershipTable.userId] = userId
            it[TenantMembershipTable.tenantId] = tenantId
            it[TenantMembershipTable.role] = role.name
            it[TenantMembershipTable.createdAt] = now
            it[TenantMembershipTable.updatedAt] = now
        } get TenantMembershipTable.id
        TenantMembership(
            id = id,
            userId = userId,
            tenantId = tenantId,
            role = role,
            createdAt = now.toInstant(),
            updatedAt = now.toInstant()
        )
    }
}
