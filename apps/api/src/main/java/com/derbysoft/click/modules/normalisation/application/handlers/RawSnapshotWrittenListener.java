package com.derbysoft.click.modules.normalisation.application.handlers;

import com.derbysoft.click.modules.ingestion.domain.events.RawSnapshotWritten;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RawSnapshotWrittenListener {

    private static final Logger log = LoggerFactory.getLogger(RawSnapshotWrittenListener.class);

    private final NormalisationService normalisationService;

    public RawSnapshotWrittenListener(NormalisationService normalisationService) {
        this.normalisationService = normalisationService;
    }

    @EventListener
    public void on(EventEnvelope<RawSnapshotWritten> envelope) {
        RawSnapshotWritten event = envelope.payload();
        log.debug("Received RawSnapshotWritten for snapshot {}", event.snapshotId());
        normalisationService.normalizeSnapshot(
            event.snapshotId(), event.integrationId(), event.tenantId(),
            event.accountId(), MappingVersion.V1
        );
    }
}
