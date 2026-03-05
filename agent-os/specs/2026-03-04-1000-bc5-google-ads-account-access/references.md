# BC5 — Reference Patterns

## Primary Reference: `channelintegration` (BC4)

All structural patterns in BC5 are adapted directly from BC4. Use BC4 files as the reference implementation.

| BC5 Concept | BC4 Reference |
|-------------|---------------|
| `GoogleConnection` aggregate | `IntegrationInstance` aggregate |
| `AccountBinding` aggregate | (new pattern, no direct BC4 equivalent) |
| `GoogleConnectionService` | `IntegrationService` |
| `DiscoverAccountsHandler` | `ScheduleService` (pattern for scheduler + error swallowing) |
| `AccountBindingService` | `IntegrationService` |
| `DiscoveryScheduleService` | `ScheduleService` |
| `GoogleAdsApiPort` | (new external API port, no BC4 equivalent) |
| `GoogleConnectionRepositoryImpl` | `IntegrationInstanceRepositoryImpl` |
| `GoogleConnectionController` | `IntegrationManagementController` |
| `ModuleRegistry` wiring | Existing ModuleRegistry |

## Key Files

```
apps/api/src/main/java/com/derbysoft/click/modules/channelintegration/
├── domain/aggregates/IntegrationInstance.java           ← aggregate pattern
├── application/handlers/IntegrationService.java         ← publishAndClear pattern
├── application/handlers/ScheduleService.java            ← @Scheduled + error swallow
├── infrastructure/persistence/repository/               ← dual-interface impl
│   └── IntegrationInstanceRepositoryImpl.java
├── interfaces/http/controller/                          ← ApiResponse wrapper
│   └── IntegrationManagementController.java
└── bootstrap/di/ModuleRegistry.java                     ← cross-BC wiring
```

## Notable BC5-Specific Patterns

- **Google Ads API Port** (`GoogleAdsApiPort`): lives in `application/ports/` — follows the same port pattern but drives an external API rather than another BC
- **AccountGraphState**: infra-only entity, no domain port — `DiscoverAccountsHandler` depends on the infra-level `AccountGraphStateRepository` interface directly
- **BindingStatus.STALE**: fourth status beyond ACTIVE/BROKEN/REMOVED — represents accounts that were in a binding but disappeared from a discovery run; recovered automatically if account reappears
