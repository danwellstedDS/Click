package com.derbysoft.click.modules.attributionmapping.application.handlers;

import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchProduced;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CanonicalBatchProducedListener {

    private static final Logger log = LoggerFactory.getLogger(CanonicalBatchProducedListener.class);

    private final AttributionService attributionService;

    public CanonicalBatchProducedListener(AttributionService attributionService) {
        this.attributionService = attributionService;
    }

    @EventListener
    public void on(EventEnvelope<CanonicalBatchProduced> envelope) {
        CanonicalBatchProduced event = envelope.payload();
        log.debug("CanonicalBatchProduced received: batchId={}, tenantId={}", event.batchId(), event.tenantId());
        attributionService.mapBatch(event.batchId(), event.tenantId(), "v1");
    }
}
