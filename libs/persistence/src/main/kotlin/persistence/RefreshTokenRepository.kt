package persistence

import domain.RefreshToken
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.ports.RefreshTokenRepositoryPort
import persistence.tables.RefreshTokenTable
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class RefreshTokenRepository : RefreshTokenRepositoryPort {

    override fun create(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken = transaction {
        val now = OffsetDateTime.now()
        val expiresAtOffset = expiresAt.atOffset(ZoneOffset.UTC)
        val id = RefreshTokenTable.insert {
            it[RefreshTokenTable.userId] = userId
            it[RefreshTokenTable.tokenHash] = tokenHash
            it[RefreshTokenTable.expiresAt] = expiresAtOffset
            it[RefreshTokenTable.createdAt] = now
        } get RefreshTokenTable.id
        RefreshToken(
            id = id,
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            createdAt = now.toInstant()
        )
    }

    override fun findByTokenHash(tokenHash: String): RefreshToken? = transaction {
        RefreshTokenTable.selectAll().where { RefreshTokenTable.tokenHash eq tokenHash }
            .singleOrNull()
            ?.let { row ->
                RefreshToken(
                    id = row[RefreshTokenTable.id],
                    userId = row[RefreshTokenTable.userId],
                    tokenHash = row[RefreshTokenTable.tokenHash],
                    expiresAt = row[RefreshTokenTable.expiresAt].toInstant(),
                    createdAt = row[RefreshTokenTable.createdAt].toInstant()
                )
            }
    }

    override fun deleteByTokenHash(tokenHash: String) = transaction {
        RefreshTokenTable.deleteWhere { RefreshTokenTable.tokenHash eq tokenHash }
        Unit
    }

    override fun deleteExpiredForUser(userId: UUID) = transaction {
        val now = OffsetDateTime.now()
        RefreshTokenTable.deleteWhere {
            (RefreshTokenTable.userId eq userId) and (RefreshTokenTable.expiresAt less now)
        }
        Unit
    }
}
