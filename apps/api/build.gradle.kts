plugins {
  java
  jacoco
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

java {
  toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

springBoot {
  mainClass.set("com.derbysoft.click.ApiApplication")
}

dependencies {
  implementation(libs.spring.boot.starter.web)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.flyway)
  implementation(libs.flyway.postgres)
  runtimeOnly(libs.postgres)
  implementation(libs.java.jwt)

  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.spring.boot.test.autoconfigure)
  testImplementation(libs.spring.boot.webmvc.test)
  testImplementation(libs.archunit)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }

tasks.jacocoTestCoverageVerification {
  violationRules {
    rule {
      limits {
        limit {
          minimum = "0.90".toBigDecimal()
        }
      }
    }
  }
}
