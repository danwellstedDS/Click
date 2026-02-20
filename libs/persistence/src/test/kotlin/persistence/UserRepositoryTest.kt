package persistence

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() = DbTestHelper.connectAndMigrate()
    }

    @BeforeEach
    fun clean() = DbTestHelper.reset()

    private val repo = UserRepository()

    @Test
    fun `findByEmail returns null when user does not exist`() {
        assertNull(repo.findByEmail("nobody@example.com"))
    }

    @Test
    fun `create and findByEmail roundtrip`() {
        val user = repo.create("alice@example.com", "hashedpassword")
        val found = repo.findByEmail("alice@example.com")
        assertNotNull(found)
        assertEquals(user.id, found.id)
        assertEquals("alice@example.com", found.email)
        assertEquals("hashedpassword", found.passwordHash)
    }

    @Test
    fun `findById returns user`() {
        val user = repo.create("bob@example.com", "hash123")
        val found = repo.findById(user.id)
        assertNotNull(found)
        assertEquals(user.id, found.id)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(repo.findById(java.util.UUID.randomUUID()))
    }
}
