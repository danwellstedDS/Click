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

## Bounded Domains (Plain Language)

Architecture is split into 12 bounded domains (BDs):

1. **BD1 — Identity & Access**: who users are and what they can do.
2. **BD2 — Tenant Governance**: tenant-level policy and guardrail decisions.
3. **BD3 — Organisation Structure**: PropertyGroup/Property hierarchy and ownership boundaries.
4. **BD4 — Channel Integration**: integration lifecycle, scheduling, and operational control plane.
5. **BD5 — Google Ads Account & Access**: MCC connection, account graph, bindings, and access health.
6. **BD6 — Google Search Ads Management**: Click-side campaign intent and async execution to provider.
7. **BD7 — Ingestion**: scheduled/manual sync jobs, retries/incidents, and raw snapshot storage.
8. **BD8 — Normalisation**: transform raw provider rows into canonical performance facts.
9. **BD9 — Attribution & Mapping**: map canonical facts to org structure with confidence and overrides.
10. **BD10 — Reporting & Portfolio Intelligence**: read models, rollups, KPIs, and coverage/freshness views.
11. **BD11 — Measurement & Attribution**: canonical intent-to-booking conversion truth and attribution outputs.
12. **BD12 — Capital Allocation & Budgeting**: budget intent, pacing, guardrails, and override governance.

High-level flow:
`BD1/BD2/BD3 -> BD4/BD5/BD6/BD7 -> BD8/BD9/BD10 -> BD11/BD12`

Detailed domain spec lives in `docs/bd.md`.

---

## Docs Index

- `docs/bd.md` — bounded domain architecture and contracts
- `docs/todo.md` — prioritized architecture/build backlog
- `docs/api-endpoints.md` — current API endpoint catalog

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

## Test Commands

API (Spring Boot):

```bash
./gradlew :apps:api:test
```

Web (Vite/React):

- No automated test script is configured yet in `apps/web/package.json`.
- Current available scripts are `dev`, `build`, and `preview`.

---

## Environment Variables

### API (used by Docker Compose and local boot)

| Variable | Required | Default (dev) | Purpose |
|---|---|---|---|
| `DATABASE_URL` | yes | `jdbc:postgresql://localhost:5432/click.5_db` (local) | JDBC connection string |
| `DB_USER` | yes | `postgres` | DB username |
| `DB_PASS` | yes | `postgres` | DB password |
| `JWT_SECRET` | yes | `dev-secret` (local) / compose fallback | JWT signing key |
| `ENV` | no | `development` (compose) | environment marker |
| `GOOGLE_ADS_DEVELOPER_TOKEN` | no (until live provider calls) | empty | Google Ads API token |

### Frontend

No mandatory runtime env vars are currently documented for `apps/web`; it relies on Vite dev proxy to `/api`.

---

## Useful endpoints

| URL | Description |
|-----|-------------|
| http://localhost:5173 | Web app |
| http://localhost:8080 | API (direct) |
| http://localhost:8081 | Adminer — DB browser |

---

## OpenAPI and Postman

- OpenAPI generation and Postman collection publishing are planned (tracked in `docs/todo.md`).
- Once available, this section should link:
  - versioned `openapi.json` / `openapi.yaml`
  - generated Postman collection
  - quick import/run guide

---

## Seed Data and Reset

### Seeded data

- Dev user: `admin@example.com` / `password`
- Database schema and baseline seed data are applied via Flyway migrations on API startup.
- BC7 mock ingestion dataset is seeded via migration:
  - `apps/api/src/main/resources/db/migration/V202603090002__seed_bc7_mock_data.sql`

### Reset local database (Docker)

```bash
docker compose down -v
docker compose up --build
```

This drops volumes, recreates Postgres, and reapplies all migrations/seeds.

---

## Troubleshooting

- API fails to start with DB errors:
  - Ensure Postgres is running and reachable on `5432`.
  - Verify `DATABASE_URL`, `DB_USER`, and `DB_PASS`.
- Migration failures on startup:
  - Check API logs for Flyway error.
  - Reset DB (`docker compose down -v`) if local schema drift is expected.
- Login/token issues:
  - Ensure `JWT_SECRET` is set and stable for the current run.
- Port conflicts (`5432`, `8080`, `8081`, `5173`):
  - Stop conflicting services or remap ports in compose/Vite config.

---

## BD Status (Current Repo Snapshot)

| BD | Status |
|---|---|
| BD1 Identity & Access | Implemented (core) |
| BD2 Tenant Governance | Implemented with partial/stubbed policy oracle integration |
| BD3 Organisation Structure | Implemented (core) |
| BD4 Channel Integration | Implemented (core) |
| BD5 Google Ads Account & Access | Implemented (core) |
| BD6 Google Search Ads Management | Implemented (active MVP scope) |
| BD7 Ingestion | Implemented (active MVP scope) |
| BD8 Normalisation | Partially implemented (MVP-1 in progress/landed; verify against latest branch state) |
| BD9 Attribution & Mapping | Not implemented (placeholder module) |
| BD10 Reporting & Portfolio Intelligence | Not implemented (placeholder module) |
| BD11 Measurement & Attribution | Spec defined, implementation pending |
| BD12 Capital Allocation & Budgeting | Spec defined, implementation pending |

---

## Spec Driven Development

This project follows Product OS / Agent OS conventions.
Install guide: https://buildermethods.com/agent-os/installation
