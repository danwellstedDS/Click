# BC5 — Relevant Standards Excerpts

## Bounded Context Structure

Each bounded context follows hexagonal architecture:
```
modules/<bc-name>/
├── api/           — public ports and contracts for other BCs
├── application/   — use cases, handlers, services
├── domain/        — aggregates, entities, value objects, events, repo ports
├── infrastructure/— persistence, external API adapters
└── interfaces/    — HTTP controllers, DTOs
```

## Aggregate Rules
- Aggregates expose a `getEvents()` / `clearEvents()` pair
- Factory methods emit the creation event; mutations emit change events
- Aggregates are reconstituted via `reconstitute(...)` — no events emitted
- Status transitions are guarded; violations throw `DomainError.Conflict` or `DomainError.ValidationError`

## Repository Pattern
- Domain ports are interfaces in `domain/`
- Infrastructure impls are in `infrastructure/persistence/repository/`
- Impls are declared as `@Bean` in `ModuleRegistry` — NOT annotated with `@Repository`
- Dual-interface impls (domain port + public API port) are explicitly documented in ModuleRegistry

## Service Pattern
- `@Service @Transactional` for all command services
- `publishAndClear(aggregate)` pattern: publish all pending events then `clearEvents()`
- Events are wrapped in `EventEnvelope.of(eventType, payload)` before publishing

## Controller Pattern
- `@RestController @RequestMapping("/api/v1/...")`
- Responses wrapped in `ApiResponse.success(data, requestId(httpRequest))`
- 201 for create, 202 for async trigger, 204 for delete, 200 otherwise
- `DomainError` exceptions are handled globally by an existing exception handler

## Test Patterns
- `@ExtendWith(MockitoExtension.class)` for pure unit tests (no Spring context)
- `@WebMvcTest(Controller.class) @AutoConfigureMockMvc(addFilters = false)` for controller slices
- `@MockitoBean` (Spring Boot 4) for mock dependencies in controller tests
- Domain aggregate tests use no mocks — just instantiate and assert
