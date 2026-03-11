# BC7 → BC5 Auth-Failure Canonical Event Contract Fix

## Context

BC7 (`JobExecutor`) published `ingestion.domain.events.AuthFailureDetected` when an `IngestionAuthException` was caught. BC5 (`AccessFailureObservedHandler`) listened for `EventEnvelope<AccessFailureObserved>` from `campaignexecution.domain.events` — the BC6 path. Spring's `ResolvableTypeProvider` routes by exact generic type, so BC7 auth failures silently never reached BC5, and Google Ads connection health was never marked broken from ingestion failures.

## Chosen Approach — Option B (canonical event, BC5 owns the contract)

- Created `AccessFailureObserved` in `googleadsmanagement.api.events` (BC5 owns the cross-BC signal contract)
- BC6 (`WriteActionExecutor`) and BC7 (`JobExecutor`) both emit from this canonical location
- BC5 (`AccessFailureObservedHandler`) updated its import — handler logic unchanged
- Deleted `campaignexecution.domain.events.AccessFailureObserved` stub
- Deleted `ingestion.domain.events.AuthFailureDetected` (replaced by canonical event)

## Files Changed

| File | Change |
|------|--------|
| `googleadsmanagement/api/events/AccessFailureObserved.java` | **Created** — canonical event |
| `googleadsmanagement/application/handlers/AccessFailureObservedHandler.java` | Updated import only |
| `campaignexecution/application/handlers/WriteActionExecutor.java` | Updated import only |
| `ingestion/application/handlers/JobExecutor.java` | Swapped `AuthFailureDetected` → `AccessFailureObserved` |
| `campaignexecution/domain/events/AccessFailureObserved.java` | **Deleted** |
| `ingestion/domain/events/AuthFailureDetected.java` | **Deleted** |
| `ingestion/application/JobExecutorTest.java` | Updated test method name and comment |
| `docs/todo.md` | Marked item #7 done |

## Canonical Event Shape

```java
package com.derbysoft.click.modules.googleadsmanagement.api.events;

import java.util.UUID;

public record AccessFailureObserved(
    UUID tenantId,
    String customerId,
    String reason
) {}
```

Fields match the previous BC6 stub exactly — only the package changes.
