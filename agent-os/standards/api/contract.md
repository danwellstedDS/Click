# API Contract

All API routes are versioned and tenant-scoped.

- Prefix all routes with `/api/v1`
- Every response is:
  - `{ success: true, data: ..., meta?: ... }` OR
  - `{ success: false, error: ..., meta?: ... }`
- Responses include `meta.requestId`
- Additive changes only within v1

## Success

```json
{ "success": true, "data": {}, "meta": { "requestId": "..." } }
```

## Error

```json
{ "success": false, "error": { "code": "VAL_001", "message": "..." }, "meta": { "requestId": "..." } }
```
