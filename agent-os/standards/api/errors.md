# Error Responses

Error codes are stable and grouped by prefix.

## Prefixes

- `VAL_` (400) validation
- `AUTH_` (401/403) auth/authz
- `RES_` (404) not found
- `CONFLICT_` (409) conflict
- `RATE_` (429) rate limit
- `UP_` (502/503) upstream provider failure
- `SYS_` (500) unexpected

## Rules

- Return safe messages only
- Log full error server-side with `requestId` and `tenantId`
- Always return both `code` and `message`
