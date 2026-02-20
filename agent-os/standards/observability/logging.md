# Logging

Structured JSON logs only.

Each request log must include:

- `requestId`
- `tenantId`
- `route`
- `status`
- `durationMs`

Return `requestId` in API `meta`.
