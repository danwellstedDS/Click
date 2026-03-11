package com.derbysoft.click.modules.googleadsmanagement.application.handlers;

import com.derbysoft.click.modules.googleadsmanagement.api.events.AccessFailureObserved;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * BC5 handler for AccessFailureObserved events emitted by BC6.
 * Marks the Google Ads connection as broken so BC5 can surface
 * the broken state to operators.
 */
@Component
public class AccessFailureObservedHandler {

    private static final Logger log = LoggerFactory.getLogger(AccessFailureObservedHandler.class);

    private final GoogleConnectionService googleConnectionService;

    public AccessFailureObservedHandler(GoogleConnectionService googleConnectionService) {
        this.googleConnectionService = googleConnectionService;
    }

    @EventListener
    public void handle(EventEnvelope<AccessFailureObserved> envelope) {
        AccessFailureObserved event = envelope.payload();
        log.warn("Access failure observed for tenant={} customerId={}: {}",
            event.tenantId(), event.customerId(), event.reason());

        googleConnectionService.findByTenantId(event.tenantId())
            .ifPresent(connection ->
                googleConnectionService.markBroken(connection.getId(), event.reason())
            );
    }
}
