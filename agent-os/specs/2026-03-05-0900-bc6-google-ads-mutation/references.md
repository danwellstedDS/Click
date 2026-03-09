# BC6 Reference Implementations

| Reference | Purpose |
|-----------|---------|
| `modules/ingestion/domain/aggregates/SyncJob.java` | Aggregate pattern for `WriteAction` |
| `modules/ingestion/domain/aggregates/SyncIncident.java` | Mirror for `ExecutionIncident` |
| `modules/ingestion/application/handlers/JobExecutor.java` | Mirror for `WriteActionExecutor` |
| `modules/ingestion/infrastructure/persistence/repository/SyncIncidentRepositoryImpl.java` | Dual-interface for `WriteActionRepositoryImpl` |
| `modules/ingestion/application/handlers/RetryPolicyEngine.java` | Mirror for BC6 retry |
| `modules/ingestion/application/handlers/IncidentLifecycleService.java` | Mirror for incident lifecycle |
| `modules/googleadsmanagement/api/ports/GoogleAdsQueryPort.java` | Query port for connection/bindings |
| `bootstrap/di/ModuleRegistry.java` | Only existing file modified — add BC6 beans |
