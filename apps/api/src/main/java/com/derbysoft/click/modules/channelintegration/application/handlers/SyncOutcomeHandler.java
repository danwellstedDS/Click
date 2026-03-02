package com.derbysoft.click.modules.channelintegration.application.handlers;

import com.derbysoft.click.modules.channelintegration.domain.events.SyncFailed;
import com.derbysoft.click.modules.channelintegration.domain.events.SyncSucceeded;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles sync outcome events originating from BC7 (Ingestion).
 * Updates IntegrationInstance health or transitions state accordingly.
 */
@Component
public class SyncOutcomeHandler {

    private static final Logger log = LoggerFactory.getLogger(SyncOutcomeHandler.class);

    private final IntegrationService integrationService;

    public SyncOutcomeHandler(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @EventListener
    public void handleSyncSucceeded(EventEnvelope<SyncSucceeded> envelope) {
        SyncSucceeded event = envelope.payload();
        log.debug("Sync succeeded for integration {} syncRun {}", event.integrationId(), event.syncRunId());
        try {
            integrationService.recordSyncSuccess(event.integrationId());
        } catch (Exception e) {
            log.warn("Could not update health after sync success for integration {}: {}",
                event.integrationId(), e.getMessage());
        }
    }

    @EventListener
    public void handleSyncFailed(EventEnvelope<SyncFailed> envelope) {
        SyncFailed event = envelope.payload();
        log.warn("Sync failed for integration {} syncRun {}: {}",
            event.integrationId(), event.syncRunId(), event.reason());
        try {
            integrationService.markBroken(event.integrationId(), event.reason());
        } catch (Exception e) {
            log.warn("Could not mark integration {} as broken after sync failure: {}",
                event.integrationId(), e.getMessage());
        }
    }
}
