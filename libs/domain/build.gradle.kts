plugins {
  java
}

java {
  toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

dependencies {
  testImplementation(libs.junit.api)
  testImplementation(libs.assertj)
  testRuntimeOnly(libs.junit.engine)
}

tasks.test { useJUnitPlatform() }
