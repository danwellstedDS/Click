# Async Jobs

Operations >3 seconds must be async.

- API starts job and returns `jobId`
- Jobs are tenant-scoped
- Jobs are idempotent
- Track: `status`, `attemptCount`, `lastErrorCode`
