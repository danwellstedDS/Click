# Click.5

Marketing services platform — Java/Spring Boot API + React web frontend.

```
apps/
  api/    Spring Boot REST API (Java 25, Gradle)
  web/    React 18 + Vite frontend
infra/
  db/migrations/   Flyway SQL migrations (applied automatically on startup)
```

---

## Quick Start

The fastest way to get up and running is Docker Compose — it starts Postgres, runs all migrations, and serves the API. You then run the frontend locally for hot-reload.

### Prerequisites

| Tool | Version |
|------|---------|
| Docker + Docker Compose | any recent |
| Node.js | 18+ |
| Java (only if running API locally) | 25 |

### 1. Start the backend

```bash
docker compose up --build
```

This starts:
- **Postgres** on `localhost:5432` (db: `project_db`, user/pass: `postgres`)
- **API** on `localhost:8080` (migrations run automatically)
- **Adminer** (DB browser) on `localhost:8081`

Wait until you see `Started ApiApplication` in the logs before proceeding.

### 2. Start the frontend

```bash
cd apps/web
npm install
npm run dev
```

The dev server starts at **http://localhost:5173** and proxies all `/api` requests to `localhost:8080`.

### 3. Log in

Open http://localhost:5173 and use the seeded dev account:

| Field | Value |
|-------|-------|
| Email | `admin@example.com` |
| Password | `password` |

You'll land on the main app as **Demo Admin** in the **Demo Organization** with full admin access.

---

## What you can explore

- **Properties** — browse the hotel property list (sidebar nav)
- **Users** — manage user accounts
- **Avatar menu** (top-right) — Profile link and Sign out

---

## Running the API locally (without Docker)

If you'd prefer to run the API outside Docker (e.g. for faster iteration with your IDE):

1. Start Postgres separately (Docker or your local install):
   ```bash
   docker compose up postgres
   ```

2. Create the database if it doesn't exist:
   ```bash
   createdb -h localhost -U postgres click.5_db
   ```

3. Run the API:
   ```bash
   DATABASE_URL=jdbc:postgresql://localhost:5432/click.5_db \
   DB_USER=postgres \
   DB_PASS=postgres \
   JWT_SECRET=dev-secret \
   ./gradlew :apps:api:bootRun
   ```

---

## Useful endpoints

| URL | Description |
|-----|-------------|
| http://localhost:5173 | Web app |
| http://localhost:8080 | API (direct) |
| http://localhost:8081 | Adminer — DB browser |

---

## Spec Driven Development

This project follows Product OS / Agent OS conventions.
Install guide: https://buildermethods.com/agent-os/installation
