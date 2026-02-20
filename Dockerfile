# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

# Cache dependency resolution separately from source compilation
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY libs/domain/build.gradle.kts libs/domain/build.gradle.kts
COPY libs/persistence/build.gradle.kts libs/persistence/build.gradle.kts
COPY apps/api/build.gradle.kts apps/api/build.gradle.kts
RUN gradle dependencies --no-daemon || true

# Copy source and build distribution
COPY . .
RUN gradle :apps:api:installDist --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/apps/api/build/install/api .
COPY infra/db/migrations infra/db/migrations
EXPOSE 8080
CMD ["bin/api"]
