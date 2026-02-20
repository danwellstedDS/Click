# Google Ads Integration

Wrap GAds behind `GoogleAdsClient` interface.

All calls must:

- have timeouts
- retry transient errors
- normalize errors to `UP_GADS_*`

- Never store refresh tokens in plaintext
- Log provider calls with `tenantId`, `requestId`, `durationMs`
