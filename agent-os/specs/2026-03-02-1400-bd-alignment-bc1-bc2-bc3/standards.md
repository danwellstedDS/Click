# Standards Snapshot — BD Alignment BC1/BC2/BC3

Extracted from `agent-os/standards/backend/standards.md` — sections relevant to UL, DDD, and domain events.

## Ubiquitous Language (UL)

All types, methods, and variables must use the canonical names from the BD spec:
- `TenantMembership` (not `OrgMembership`)
- `ActorContext` (not `AuthClaims`)
- `tenantId` (not `organizationId`)
- `role: Role` (not `isOrgAdmin: boolean`)
- `PropertyGroup` (not `Hotel`, `Chain`, or `Organization` in BC1/BC3 contexts)

## Domain Layer Rules

- Pure Java: `record` value objects, `final` class aggregates/entities
- No JPA / Spring annotations in `domain/`
- Repository port interfaces in `domain/` (no persistence dependencies)
- Domain events in `domain/events/` — plain `record` types

## Domain Events Pattern (per BC4 reference)

```java
// In aggregate — event accumulation
private final List<Object> events = new ArrayList<>();

public List<Object> getEvents() {
    return Collections.unmodifiableList(events);
}

public void clearEvents() {
    events.clear();
}
```

Application handler publishes after save:
```java
instance.getEvents().forEach(event ->
    eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
);
instance.clearEvents();
```

## Aggregate Factory Pattern

```java
// Factory (new instance, emits events)
public static Aggregate create(...) {
    Aggregate a = new Aggregate(...);
    a.events.add(new SomethingCreated(...));
    return a;
}

// Reconstitute (from persistence, no events)
public static Aggregate reconstitute(...) {
    return new Aggregate(...);
}
```

## Cross-BC Rules (ArchUnit)

- BC1 domain (`identityaccess.domain..`) must NOT reference BC3 (`organisationstructure..`)
- BC1 application/infrastructure may reference BC3 `api/` only (ports + contracts)
- After this refactoring: BC1 application no longer references BC3 at all
