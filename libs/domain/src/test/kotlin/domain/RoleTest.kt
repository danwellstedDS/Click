package domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RoleTest {

    @Test
    fun `valueOf returns ADMIN`() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"))
    }

    @Test
    fun `valueOf returns VIEWER`() {
        assertEquals(Role.VIEWER, Role.valueOf("VIEWER"))
    }

    @Test
    fun `valueOf throws on unknown role`() {
        assertThrows<IllegalArgumentException> {
            Role.valueOf("SUPERUSER")
        }
    }

    @Test
    fun `all roles are enumerated`() {
        assertEquals(2, Role.entries.size)
    }
}
