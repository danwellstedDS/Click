package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.modules.channelintegration.domain.events.SyncRequested;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SyncRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(SyncRequestedListener.class);

    private final IngestionJobService ingestionJobService;

    public SyncRequestedListener(IngestionJobService ingestionJobService) {
        this.ingestionJobService = ingestionJobService;
    }

    @EventListener
    public void onSyncRequested(EventEnvelope<SyncRequested> envelope) {
        SyncRequested e = envelope.payload();
        log.debug("SyncRequested received for integration {} tenant {}",
            e.integrationId(), e.tenantId());
        try {
            ingestionJobService.enqueueDailySync(e.integrationId(), e.tenantId());
        } catch (Exception ex) {
            log.warn("Failed to enqueue daily sync for integration {} tenant {}: {}",
                e.integrationId(), e.tenantId(), ex.getMessage());
        }
    }
}
