rootProject.name = "click.5"

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

include(
  ":apps:api",
  ":libs:domain",
  ":libs:persistence"
)
