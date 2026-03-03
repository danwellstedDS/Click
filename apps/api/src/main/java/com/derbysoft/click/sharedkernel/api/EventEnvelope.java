package com.derbysoft.click.sharedkernel.api;

import java.time.Instant;
import java.util.UUID;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * Generic event envelope for in-process domain event publishing.
 *
 * <p>Implements {@link ResolvableTypeProvider} so that Spring's {@link
 * org.springframework.context.ApplicationEventPublisher} can resolve the actual generic type {@code
 * T} at runtime (despite type erasure) and route events only to the correctly-typed {@code
 * @EventListener} methods. Without this, every {@code EventEnvelope<?>} publish would trigger all
 * {@code @EventListener} methods that accept any {@code EventEnvelope<*>}, causing a {@link
 * ClassCastException} when the payload type doesn't match the listener's expected type.
 */
public record EventEnvelope<T>(
    UUID eventId,
    String eventType,
    Instant occurredAt,
    T payload
) implements ResolvableTypeProvider {

  public static <T> EventEnvelope<T> of(String eventType, T payload) {
    return new EventEnvelope<>(UUID.randomUUID(), eventType, Instant.now(), payload);
  }

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        EventEnvelope.class,
        payload != null ? ResolvableType.forInstance(payload) : ResolvableType.forClass(Object.class)
    );
  }
}
