package persistence

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RefreshTokenRepositoryTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() = DbTestHelper.connectAndMigrate()
    }

    @BeforeEach
    fun clean() = DbTestHelper.reset()

    private val userRepo = UserRepository()
    private val tokenRepo = RefreshTokenRepository()

    @Test
    fun `create and findByTokenHash`() {
        val user = userRepo.create("tok@example.com", "hash")
        val expiresAt = Instant.now().plusSeconds(3600)
        tokenRepo.create(user.id, "testhash123", expiresAt)

        val found = tokenRepo.findByTokenHash("testhash123")
        assertNotNull(found)
    }

    @Test
    fun `findByTokenHash returns null for unknown hash`() {
        assertNull(tokenRepo.findByTokenHash("nonexistent"))
    }

    @Test
    fun `deleteByTokenHash removes token`() {
        val user = userRepo.create("del@example.com", "hash")
        tokenRepo.create(user.id, "deleteme", Instant.now().plusSeconds(3600))

        tokenRepo.deleteByTokenHash("deleteme")
        assertNull(tokenRepo.findByTokenHash("deleteme"))
    }

    @Test
    fun `deleteExpiredForUser removes only expired tokens`() {
        val user = userRepo.create("exp@example.com", "hash")
        val past = Instant.now().minusSeconds(3600)
        val future = Instant.now().plusSeconds(3600)

        tokenRepo.create(user.id, "expiredhash", past)
        tokenRepo.create(user.id, "validhash", future)

        tokenRepo.deleteExpiredForUser(user.id)

        assertNull(tokenRepo.findByTokenHash("expiredhash"))
        assertNotNull(tokenRepo.findByTokenHash("validhash"))
    }
}
