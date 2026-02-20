package persistence

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

data class DbConfig(val jdbcUrl: String, val user: String, val password: String)

object Db {
  fun connectAndMigrate(cfg: DbConfig) {
    Flyway.configure()
      .dataSource(cfg.jdbcUrl, cfg.user, cfg.password)
      .locations("filesystem:infra/db/migrations")
      .load()
      .migrate()

    Database.connect(
      url = cfg.jdbcUrl,
      driver = "org.postgresql.Driver",
      user = cfg.user,
      password = cfg.password
    )
  }
}
