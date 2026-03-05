# BC5 Google Ads Account & Access — Implementation Plan

## Context

BC4 delivered chain CRUD, org assignment, and the context switcher. BC5 introduces the Google Ads provider layer: connecting an MCC (manager) account, discovering its accessible client accounts via the Google Ads API, and managing explicit `AccountBinding` records that determine which Google Ads accounts a tenant can use for campaigns.

This is a new bounded context `googleadsmanagement` following the `channelintegration` module exactly as its reference pattern.

**Key facts from exploration:**
- Service account JSON: `infra/secrets/google-search-creds.json` (project `click-ppc`, SA `google-ads-click-ppc@click-ppc.iam.gserviceaccount.com`)
- No Google Ads library in deps yet — must add `com.google.api-ads:google-ads`
- Latest migration: `V202603030003__add_property_group_status.sql` → BC5 starts at `V202603040001`
- Test managerID: `858-270-7576`; test account: `506-204-8043`

---

## Execution Order

### Task 1 — Save spec documentation ✅

Created `agent-os/specs/2026-03-04-1000-bc5-google-ads-account-access/` with:
- `plan.md` — this full plan
- `shape.md` — shaping notes and decisions
- `standards.md` — relevant standards excerpts
- `references.md` — pointers to `channelintegration` as the reference pattern

---

### Task 2 — Add Google Ads dependency ✅

Added `com.google.api-ads:google-ads:35.0.0` to `libs.versions.toml`, `build.gradle.kts`, and `application.yml`.

---

### Task 3 — Domain Layer ✅

Created in `com.derbysoft.click.modules.googleadsmanagement.domain`:
- Value objects: `ConnectionStatus`, `BindingStatus`, `BindingType`
- Aggregates: `GoogleConnection`, `AccountBinding`
- Entity: `AccountGraphState`
- Domain ports: `GoogleConnectionRepository`, `AccountBindingRepository`
- 12 domain events

---

### Task 4 — Application Layer ✅

Created in `com.derbysoft.click.modules.googleadsmanagement.application`:
- Port: `GoogleAdsApiPort` (with `DiscoveredAccount` record)
- Services: `GoogleConnectionService`, `AccountBindingService`
- Handlers: `DiscoverAccountsHandler`
- Scheduler: `DiscoveryScheduleService`

---

### Task 5 — Infrastructure: Google Ads ACL ✅

Created in `com.derbysoft.click.modules.googleadsmanagement.infrastructure.googleads`:
- `GoogleAdsConfig` — `@ConfigurationProperties(prefix = "google.ads")`
- `GoogleAdsApiClient` — implements `GoogleAdsApiPort`, uses v35 library

---

### Task 6 — Infrastructure: Persistence ✅

Created in `com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence`:
- 3 JPA entities: `GoogleConnectionEntity`, `AccountGraphStateEntity`, `AccountBindingEntity`
- 3 Spring Data JPA repos: `GoogleConnectionJpaRepository`, `AccountGraphStateJpaRepository`, `AccountBindingJpaRepository`
- Infra interface: `AccountGraphStateRepository` (used directly by `DiscoverAccountsHandler`)
- 3 mappers: `GoogleConnectionMapper`, `AccountBindingMapper`, `AccountGraphStateMapper`
- 3 repo impls: `GoogleConnectionRepositoryImpl` (dual-interface: domain + query port), `AccountBindingRepositoryImpl`, `AccountGraphStateRepositoryImpl`

---

### Task 7 — Database Migrations ✅

- `V202603040001__create_google_connections.sql`
- `V202603040002__create_account_graph_state.sql`
- `V202603040003__create_account_bindings.sql`
- `V202603040004__seed_google_connections.sql` — seeds MCC `858-270-7576`, account `506-204-8043`, tenant `00000000-0000-0000-0000-000000000001`

---

### Task 8 — Interfaces Layer ✅

Created in `com.derbysoft.click.modules.googleadsmanagement.interfaces.http`:
- Controllers: `GoogleConnectionController`, `AccountBindingController`
- 6 DTOs: `CreateConnectionRequest`, `RotateCredentialRequest`, `CreateBindingRequest`, `ConnectionResponse`, `AccountGraphStateResponse`, `BindingResponse`

---

### Task 9 — Module Wiring ✅

Updated `ModuleRegistry` with 3 new `@Bean` declarations for BC5 repo impls.

---

### Task 10 — Tests ✅

7 test classes created:
- `GoogleConnectionTest` — 5 domain aggregate tests
- `AccountBindingTest` — 6 domain aggregate tests
- `GoogleConnectionServiceTest` — 3 application service tests
- `DiscoverAccountsHandlerTest` — 5 discovery handler tests
- `AccountBindingServiceTest` — 4 application service tests
- `GoogleConnectionControllerTest` — 5 `@WebMvcTest` tests
- `AccountBindingControllerTest` — 4 `@WebMvcTest` tests

---

## Reference Patterns

| Pattern | File |
|---------|------|
| Aggregate with event list | `channelintegration/domain/aggregates/IntegrationInstance.java` |
| `publishAndClear()` | `channelintegration/application/handlers/IntegrationService.java` |
| `@Scheduled` polling | `channelintegration/application/handlers/ScheduleService.java` |
| JPA entity + mapper | `channelintegration/infrastructure/persistence/` |
| Dual-interface repository impl | `channelintegration/infrastructure/persistence/repository/IntegrationInstanceRepositoryImpl.java` |
| Controller + ApiResponse | `channelintegration/interfaces/http/controller/IntegrationManagementController.java` |
| Module wiring | `bootstrap/di/ModuleRegistry.java` |
