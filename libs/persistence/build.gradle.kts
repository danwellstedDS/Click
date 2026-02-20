plugins {
  java
  alias(libs.plugins.spring.dependency.management)
}

java {
  toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
  }
}

dependencies {
  implementation(project(":libs:domain"))

  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.flyway)
  implementation(libs.flyway.postgres)
  implementation(libs.postgres)

  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation(libs.testcontainers.junit)
  testImplementation(libs.testcontainers.pg)
}

tasks.test { useJUnitPlatform() }
