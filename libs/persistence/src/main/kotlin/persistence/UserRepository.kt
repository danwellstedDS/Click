package persistence

import domain.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.ports.UserRepositoryPort
import persistence.tables.UserTable
import java.time.OffsetDateTime
import java.util.UUID

class UserRepository : UserRepositoryPort {

    override fun findByEmail(email: String): User? = transaction {
        UserTable.selectAll().where { UserTable.email eq email }
            .singleOrNull()
            ?.let { row ->
                User(
                    id = row[UserTable.id],
                    email = row[UserTable.email],
                    passwordHash = row[UserTable.passwordHash],
                    createdAt = row[UserTable.createdAt].toInstant(),
                    updatedAt = row[UserTable.updatedAt].toInstant()
                )
            }
    }

    override fun findById(id: UUID): User? = transaction {
        UserTable.selectAll().where { UserTable.id eq id }
            .singleOrNull()
            ?.let { row ->
                User(
                    id = row[UserTable.id],
                    email = row[UserTable.email],
                    passwordHash = row[UserTable.passwordHash],
                    createdAt = row[UserTable.createdAt].toInstant(),
                    updatedAt = row[UserTable.updatedAt].toInstant()
                )
            }
    }

    override fun create(email: String, passwordHash: String): User = transaction {
        val now = OffsetDateTime.now()
        val id = UserTable.insert {
            it[UserTable.email] = email
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.createdAt] = now
            it[UserTable.updatedAt] = now
        } get UserTable.id
        User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            createdAt = now.toInstant(),
            updatedAt = now.toInstant()
        )
    }
}
