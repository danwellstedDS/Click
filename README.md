# Click.5

Java/Spring Boot monorepo:
- Spring Boot API: apps/api
- React web: apps/web
- Flyway migrations: infra/db/migrations
- Postgres/Redis via shared dev infra

## Prereqs
- Shared infra running: ~/00_System/dev-infra
- Java 25

## Run (local)
1) Create DB (once):
   createdb -h localhost -U postgres click.5_db

2) Run API:
   DATABASE_URL=jdbc:postgresql://localhost:5432/click.5_db \
   DB_USER=postgres DB_PASS=postgres \
   JWT_SECRET=dev-secret \
   ./gradlew :apps:api:bootRun

3) Run web:
   cd apps/web
   npm i
   npm run dev

## Spec Driven Development
- Based on Product OS
- Follow install guide here https://buildermethods.com/agent-os/installation