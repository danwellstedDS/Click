# ---- Build stage ----
# Use gradle:8-jdk21 so the Gradle daemon runs on a supported JVM (≤24).
# The foojay toolchain resolver in settings.gradle.kts will provision JDK 25
# automatically for the compilation toolchain.
FROM gradle:8-jdk21 AS builder
WORKDIR /app

COPY . .
RUN gradle :apps:api:bootJar --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/apps/api/build/libs/*.jar app.jar
COPY infra/db/migrations infra/db/migrations
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
