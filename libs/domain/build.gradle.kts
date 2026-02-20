plugins {
  alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(21) }

dependencies {
  testImplementation(libs.kotlin.test)
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
}

tasks.test { useJUnitPlatform() }
