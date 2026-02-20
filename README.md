# click.5

Kotlin-first prototype monorepo:
- Ktor API: apps/api
- React web: apps/web
- Flyway migrations: infra/db/migrations
- Postgres/Redis via shared dev infra

## Prereqs
- Shared infra running: ~/00_System/dev-infra

## Run (local)
1) Create DB (once):
   createdb -h localhost -U postgres click.5_db

2) Run API:
   DATABASE_URL=jdbc:postgresql://localhost:5432/click.5_db \
   DB_USER=postgres DB_PASS=postgres \
   ./gradlew :apps:api:run

3) Run web:
   cd apps/web
   npm i
   npm run dev
