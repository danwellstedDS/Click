package persistence.ports

import domain.RefreshToken
import java.time.Instant
import java.util.UUID

interface RefreshTokenRepositoryPort {
    fun create(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun deleteByTokenHash(tokenHash: String)
    fun deleteExpiredForUser(userId: UUID)
}
