package com.derbysoft.click.bootstrap.messaging;

import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Minimal in-process event bus backed by Spring's ApplicationEventPublisher.
 * Publish events here for synchronous in-process delivery.
 * Replace with a durable broker (Kafka, RabbitMQ) when cross-process messaging is needed.
 */
@Component
public class InProcessEventBus {

  private final ApplicationEventPublisher publisher;

  public InProcessEventBus(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public <T> void publish(EventEnvelope<T> event) {
    publisher.publishEvent(event);
  }
}
