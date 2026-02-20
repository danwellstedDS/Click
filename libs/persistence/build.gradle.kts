plugins {
  alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(21) }

dependencies {
  implementation(project(":libs:domain"))

  implementation(libs.exposed.core)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  implementation(libs.postgres)
  implementation(libs.flyway)
  implementation(libs.exposed.java.time)
  implementation(libs.flyway.postgres)

  testImplementation(libs.testcontainers.junit)
  testImplementation(libs.testcontainers.pg)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
}

tasks.test { useJUnitPlatform() }
