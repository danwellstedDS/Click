package api

import api.plugins.RequestIdPlugin
import api.plugins.configureJwt
import api.routes.authRoutes
import domain.Health
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import persistence.Db
import persistence.DbConfig
import persistence.RefreshTokenRepository
import persistence.TenantMembershipRepository
import persistence.UserRepository

fun main() {
    val port = (System.getenv("PORT") ?: "8080").toInt()
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET environment variable is required")
    val env = System.getenv("ENV") ?: "development"
    val isProduction = env == "production"

    val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/project_db"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPass = System.getenv("DB_PASS") ?: "postgres"

    Db.connectAndMigrate(DbConfig(jdbcUrl, dbUser, dbPass))

    val userRepo = UserRepository()
    val membershipRepo = TenantMembershipRepository()
    val refreshTokenRepo = RefreshTokenRepository()

    embeddedServer(Netty, port = port) {
        install(RequestIdPlugin)
        install(ContentNegotiation) { jackson() }
        configureJwt(jwtSecret)

        routing {
            get("/health") { call.respond(Health()) }
            get("/api/health") { call.respond(Health()) }

            authRoutes(userRepo, membershipRepo, refreshTokenRepo, jwtSecret, isProduction)
        }
    }.start(wait = true)
}
