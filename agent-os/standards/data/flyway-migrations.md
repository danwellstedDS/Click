# Flyway Migrations

Naming: `V{YYYYMMDDHHMM}__description.sql`

- Never edit applied migrations
- Every migration must include required indexes
- CI must run: fresh DB → migrate → start app
