plugins {
  alias(libs.plugins.kotlin.jvm)
  application
}

kotlin { jvmToolchain(21) }

application {
  mainClass.set("api.MainKt")
}

dependencies {
  implementation(project(":libs:domain"))
  implementation(project(":libs:persistence"))

  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.contentnegotiation)
  implementation(libs.ktor.serialization.jackson)
  implementation(libs.ktor.server.calllogging)
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.auth.jwt)
  implementation(libs.bcrypt)

  implementation(libs.logback)

  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
}

tasks.test { useJUnitPlatform() }
