# Error Responses

Use error codes: `AUTH_001`, `DB_001`, `VAL_001`

```json
{ "success": false, "error": { "code": "AUTH_001", "message": "..." } }
```

- Always include both code and message
- Log full error server-side, return safe message to client