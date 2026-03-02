# BC4 Channel Integration — Standards Applied

## Backend Architecture Standards

### Hexagonal Architecture (Ports & Adapters)
- Domain layer: pure business logic, no framework dependencies
- Application layer: orchestrates domain + ports
- Infrastructure layer: JPA adapters, mappers
- Interfaces layer: HTTP controllers, DTOs
- API layer: public contracts for cross-BC communication

### Bounded Context Rules
- Each BC can only depend on another BC's `api/` package
- Never import another BC's domain, infrastructure, application, or interfaces
- Enforced by ArchUnit `BoundaryRulesTest`
- Cross-BC wiring is explicit in `bootstrap/di/ModuleRegistry.java`

### Aggregate Design
- Single aggregate root per bounded context operation
- Factory method (`static create()`) for construction
- Domain methods enforce business invariants before state change
- Events captured in `List<Object> events`, cleared after publish
- `updatedAt` set on every state change

### Error Handling
- `DomainError.ValidationError` — invalid input / constraint violation
- `DomainError.Conflict` — invalid state transition
- `DomainError.NotFound` — entity not found
- `DomainError.Forbidden` — governance/auth rejection
- All extend `DomainError` (sealed class), mapped in `ErrorHandlingAdvice`

### Event Publishing
- `InProcessEventBus.publish(EventEnvelope.of(eventType, payload))`
- Events published after aggregate saved to DB
- `aggregate.clearEvents()` called after publishing
- Ready for async/durable broker replacement

### Repository Pattern
- Domain interface in `domain/`
- JPA implementation in `infrastructure/persistence/repository/`
- JPA implementation can dual-implement domain port + query port
- Registered in `ModuleRegistry` for explicit cross-BC wiring

### Controller Pattern
- `@RestController @RequestMapping("/api/v1/<resource>")`
- Uses `ApiResponse<T>` wrapper for all responses
- `requestId` extracted from `HttpServletRequest` attribute
- Error handling delegated to `ErrorHandlingAdvice`

## API Spec Standards

### Response Format
```json
{ "success": true, "data": {...}, "error": null, "meta": { "requestId": "..." } }
```

### HTTP Status Codes
- 200 OK — successful read/update
- 201 Created — successful creation
- 202 Accepted — async operation initiated
- 204 No Content — successful deletion
- 400 Bad Request — ValidationError
- 404 Not Found — NotFound
- 409 Conflict — Conflict (invalid state transition)
- 403 Forbidden — Forbidden

## Workflow Standards

### Branch Naming
`feature/<scope>-<slug>` — BC4 branch: `feature/be-channel-integration-bc4`

### Commit Style
Imperative mood, concise, focused on "why" not "what"

### Testing Requirements
- Domain unit tests: pure Java, no mocks, no Spring context
- Application service tests: Mockito, `@ExtendWith(MockitoExtension.class)`
- Controller tests: `@WebMvcTest`, `@AutoConfigureMockMvc(addFilters=false)`
- ArchUnit boundary rules in `BoundaryRulesTest`
