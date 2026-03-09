# References — BC6 Phase 2

## BC7 GoogleAdsReportingClient
`apps/api/src/main/java/com/derbysoft/click/modules/ingestion/infrastructure/googleads/GoogleAdsReportingClient.java`
- `buildClient(managerId, credentialPath)` pattern — credentials from file, scoped to adwords
- `handleGoogleAdsException(e)` — iterates errors, checks errorCode strings for auth failures
- `handleStatusRuntimeException(e)` — maps gRPC Status.Code to TRANSIENT/PERMANENT

## BC6 existing port
`apps/api/src/main/java/com/derbysoft/click/modules/campaignexecution/application/ports/GoogleAdsMutationPort.java`
- 8 mutation methods with typed Spec records
- `MutationResult(boolean success, String resourceId, String failureClass, String failureReason)`

## BC6 existing stub
`apps/api/src/main/java/com/derbysoft/click/modules/campaignexecution/infrastructure/googleads/GoogleAdsMutationClient.java`
- To be replaced entirely in Phase 2

## Shared config
`apps/api/src/main/java/com/derbysoft/click/modules/googleadsmanagement/infrastructure/googleads/GoogleAdsConfig.java`
- `getCredentialsPath()`, `getDeveloperToken()`
