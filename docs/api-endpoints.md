# API Endpoints

This document lists HTTP endpoints currently implemented in `apps/api`.

## Health

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Basic service health check. |
| `GET` | `/api/health` | API-scoped health check. |

## Auth (`AuthController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Authenticate user and issue tokens/session payload. |
| `POST` | `/api/v1/auth/refresh` | Refresh access token. |
| `POST` | `/api/v1/auth/switch-tenant` | Switch active tenant context for current user. |
| `GET` | `/api/v1/auth/me` | Return current user identity/context. |
| `GET` | `/api/v1/auth/tenants` | List tenants user can access. |
| `POST` | `/api/v1/auth/logout` | Log out current session/token context. |

## User Management (`UserManagementController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/users` | List users for tenant. |
| `POST` | `/api/v1/users` | Create user. |
| `GET` | `/api/v1/users/{id}` | Get user detail. |
| `PATCH` | `/api/v1/users/{id}` | Update user role. |
| `DELETE` | `/api/v1/users/{id}` | Delete user. |

## Channel Integration (`IntegrationManagementController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/integrations` | Create integration instance. |
| `GET` | `/api/v1/integrations` | List integrations by tenant. |
| `GET` | `/api/v1/integrations/{id}` | Get integration detail. |
| `PUT` | `/api/v1/integrations/{id}/credentials` | Attach/replace integration credential. |
| `DELETE` | `/api/v1/integrations/{id}/credentials` | Detach integration credential. |
| `POST` | `/api/v1/integrations/{id}/pause` | Pause integration. |
| `POST` | `/api/v1/integrations/{id}/resume` | Resume integration. |
| `PUT` | `/api/v1/integrations/{id}/schedule` | Update integration sync schedule. |
| `POST` | `/api/v1/integrations/{id}/sync` | Trigger sync now. |
| `DELETE` | `/api/v1/integrations/{id}` | Delete integration instance. |

## Google Connections (`GoogleConnectionController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/google/connections` | Create Google connection for tenant. |
| `GET` | `/api/v1/google/connections?tenantId=...` | Get tenant Google connection. |
| `POST` | `/api/v1/google/connections/{id}/rotate-credential` | Rotate credential path for connection. |
| `POST` | `/api/v1/google/connections/{id}/discover` | Trigger account discovery. |
| `POST` | `/api/v1/google/connections/{id}/validate` | Validate configured Google credential access. |
| `GET` | `/api/v1/google/connections/{id}/accounts` | List discovered accounts for connection. |
| `DELETE` | `/api/v1/google/connections/{id}` | Remove connection (currently no-op domain delete). |

## Google Account Bindings (`AccountBindingController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/google/bindings` | Create account binding. |
| `GET` | `/api/v1/google/bindings?connectionId=...` | List bindings by connection. |
| `GET` | `/api/v1/google/bindings/resolve?tenantId=...` | Resolve active bindings for tenant. |
| `DELETE` | `/api/v1/google/bindings/{id}` | Remove binding (soft remove in domain). |

## Ingestion Jobs (`SyncJobController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/ingestion/jobs/manual` | Trigger manual sync job. |
| `POST` | `/api/v1/ingestion/jobs/backfill` | Trigger backfill sync job. |
| `POST` | `/api/v1/ingestion/jobs/force-run` | Force-run sync job. |
| `GET` | `/api/v1/ingestion/jobs` | List sync jobs for tenant. |

## Ingestion Incidents (`SyncIncidentController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/ingestion/incidents` | List open/reopened ingestion incidents. |
| `GET` | `/api/v1/ingestion/incidents/escalated` | List escalated ingestion incidents. |
| `POST` | `/api/v1/ingestion/incidents/{id}/acknowledge` | Acknowledge escalated ingestion incident. |

## Campaign Plans (`CampaignPlanController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/campaign-plans` | Create campaign plan. |
| `GET` | `/api/v1/campaign-plans?tenantId=...` | List plans by tenant. |
| `GET` | `/api/v1/campaign-plans/{planId}` | Get campaign plan detail. |
| `POST` | `/api/v1/campaign-plans/{planId}/revisions` | Save new draft revision with items. |
| `GET` | `/api/v1/campaign-plans/{planId}/revisions` | List revisions for plan. |
| `GET` | `/api/v1/campaign-plans/{planId}/revisions/{revId}` | Get revision detail. |
| `POST` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/publish` | Publish revision. |
| `POST` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/apply` | Apply published revision (async execution). |
| `POST` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/cancel` | Cancel revision. |

## Plan Items (`PlanItemController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/items` | List plan items in revision. |
| `GET` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/items/{itemId}` | Get plan item detail. |
| `POST` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/items/{itemId}/retry` | Retry failed item. |
| `POST` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/items/{itemId}/force-run` | Force-run failed/blocked item. |
| `GET` | `/api/v1/campaign-plans/{planId}/revisions/{revId}/items/{itemId}/explain` | Explain item state with related write actions. |

## Execution Queue (`WriteActionQueueController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/execution/queue?tenantId=...` | List pending execution queue entries for tenant. |
| `GET` | `/api/v1/execution/queue/{revisionId}` | List write actions by revision. |

## Execution Incidents (`ExecutionIncidentController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/execution/incidents?tenantId=...` | List open/reopened execution incidents. |
| `GET` | `/api/v1/execution/incidents/escalated?tenantId=...` | List escalated execution incidents. |
| `POST` | `/api/v1/execution/incidents/{id}/acknowledge` | Acknowledge escalated execution incident. |

## Drift Reports (`DriftReportController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/drift-reports?planId=...` | List drift reports for plan. |
| `GET` | `/api/v1/drift-reports/revision?revisionId=...` | List drift reports for revision. |

## Organisation Structure (`ChainManagementController`, `PropertyManagementController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/chains` | List chain/property-group records. |
| `POST` | `/api/v1/chains` | Create chain/property-group. |
| `GET` | `/api/v1/chains/{id}` | Get chain detail. |
| `PATCH` | `/api/v1/chains/{id}/status` | Update chain status. |
| `GET` | `/api/v1/properties` | List properties in current tenant scope. |
| `POST` | `/api/v1/properties` | Create property. |
| `DELETE` | `/api/v1/properties/{id}` | Delete property. |

## Tenant Governance (`OrganizationManagementController`)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/organizations` | List organizations in governance context. |
