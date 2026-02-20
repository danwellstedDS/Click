package persistence

import domain.Role
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import persistence.tables.TenantTable
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TenantMembershipRepositoryTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() = DbTestHelper.connectAndMigrate()
    }

    @BeforeEach
    fun clean() = DbTestHelper.reset()

    private val userRepo = UserRepository()
    private val membershipRepo = TenantMembershipRepository()

    private fun insertTenant(name: String = "Test Tenant"): UUID = transaction {
        val now = OffsetDateTime.now()
        TenantTable.insert {
            it[TenantTable.name] = name
            it[TenantTable.createdAt] = now
            it[TenantTable.updatedAt] = now
        } get TenantTable.id
    }

    @Test
    fun `create membership and findByUserId`() {
        val user = userRepo.create("member@example.com", "hash")
        val tenantId = insertTenant()
        membershipRepo.create(user.id, tenantId, Role.ADMIN)

        val memberships = membershipRepo.findByUserId(user.id)
        assertEquals(1, memberships.size)
        assertEquals(tenantId, memberships[0].tenantId)
        assertEquals(Role.ADMIN, memberships[0].role)
    }

    @Test
    fun `findByUserAndTenant returns membership`() {
        val user = userRepo.create("member2@example.com", "hash")
        val tenantId = insertTenant()
        membershipRepo.create(user.id, tenantId, Role.VIEWER)

        val found = membershipRepo.findByUserAndTenant(user.id, tenantId)
        assertNotNull(found)
        assertEquals(Role.VIEWER, found.role)
    }

    @Test
    fun `findByUserAndTenant returns null for wrong tenant`() {
        val user = userRepo.create("member3@example.com", "hash")
        val tenantId = insertTenant()
        membershipRepo.create(user.id, tenantId, Role.ADMIN)

        val otherTenantId = insertTenant("Other Tenant")
        assertNull(membershipRepo.findByUserAndTenant(user.id, otherTenantId))
    }

    @Test
    fun `isolation: memberships from other users not returned`() {
        val user1 = userRepo.create("u1@example.com", "hash")
        val user2 = userRepo.create("u2@example.com", "hash")
        val tenantId = insertTenant()
        membershipRepo.create(user1.id, tenantId, Role.ADMIN)
        membershipRepo.create(user2.id, tenantId, Role.VIEWER)

        val u1Memberships = membershipRepo.findByUserId(user1.id)
        assertEquals(1, u1Memberships.size)
        assertEquals(user1.id, u1Memberships[0].userId)
    }
}
