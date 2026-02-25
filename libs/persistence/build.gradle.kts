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
  testImplementation(libs.spring.boot.data.jpa.test)
  testImplementation(libs.spring.boot.jpa.test)
  testImplementation(libs.spring.boot.jdbc.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation(libs.testcontainers.junit)
  testImplementation(libs.testcontainers.pg)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
