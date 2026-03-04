# BC3 Chain Management — Reference Implementations

## Backend References

| Pattern | File |
|---------|------|
| Admin check + event publish | `modules/organisationstructure/application/handlers/PropertyManagementHandler.java` |
| Aggregate with domain events | `modules/channelintegration/domain/aggregates/IntegrationInstance.java` |
| Static mapper toDomain/toEntity | `modules/channelintegration/infrastructure/persistence/mapper/IntegrationInstanceMapper.java` |
| Controller with UserPrincipal + ApiResponse | `modules/organisationstructure/interfaces/http/controller/PropertyManagementController.java` |
| Domain repository interface | `modules/organisationstructure/domain/PropertyGroupRepository.java` |
| JPA repository impl | `modules/organisationstructure/infrastructure/persistence/repository/PropertyGroupRepositoryImpl.java` |

## Frontend References

| Pattern | File |
|---------|------|
| List page with table + modal | `apps/web/src/features/users/UsersListPage.tsx` |
| Route guard (admin) | `apps/web/src/features/auth/AdminRoute.tsx` |
| API client usage | `apps/web/src/lib/apiClient.ts` |
| AppLayout sidebar | `apps/web/src/components/AppLayout.tsx` |
| Route definitions | `apps/web/src/App.tsx` |

## Key Domain Event Patterns

```java
// Emit from aggregate create():
events.add(new ChainCreated(id, name, createdAt));

// Emit from state transition:
events.add(new ChainStatusChanged(id, oldStatus, newStatus, Instant.now()));

// Publish in service:
eventBus.publish(EventEnvelope.of(
    ChainCreated.class.getSimpleName(),
    chainCreatedEvent
));
```

## Key Error Patterns

```java
// Auth guard
if (principal.role() != Role.ADMIN) {
    throw new DomainError.Forbidden("AUTH_403", "Admin access required");
}

// Not found
throw new DomainError.NotFound("CHAIN_404", "Chain not found: " + id);

// Conflict (same-state transition)
throw new DomainError.Conflict("CHAIN_409", "Chain is already ACTIVE");

// Validation
throw new DomainError.ValidationError("CHAIN_400", "name is required");
```
