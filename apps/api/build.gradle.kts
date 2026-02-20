plugins {
  java
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

java {
  toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

springBoot {
  mainClass.set("api.ApiApplication")
}

dependencies {
  implementation(project(":libs:domain"))
  implementation(project(":libs:persistence"))

  implementation(libs.spring.boot.starter.web)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.java.jwt)

  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.spring.boot.test.autoconfigure)
}

tasks.test { useJUnitPlatform() }
