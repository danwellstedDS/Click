package persistence.ports

import domain.User
import java.util.UUID

interface UserRepositoryPort {
    fun findByEmail(email: String): User?
    fun findById(id: UUID): User?
    fun create(email: String, passwordHash: String): User
}
