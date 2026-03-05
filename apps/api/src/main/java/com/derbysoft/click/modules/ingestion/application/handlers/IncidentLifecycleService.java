package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.ingestion.domain.SyncIncidentRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentLifecycleService {

    private final SyncIncidentRepository incidentRepository;
    private final InProcessEventBus eventBus;

    public IncidentLifecycleService(SyncIncidentRepository incidentRepository, InProcessEventBus eventBus) {
        this.incidentRepository = incidentRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void onSuccess(String idempotencyKey, UUID tenantId) {
        Instant now = Instant.now();
        Optional<SyncIncident> existing = incidentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            SyncIncident incident = existing.get();
            incident.autoClose(now);
            SyncIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        }
    }

    @Transactional
    public void onFailure(String idempotencyKey, UUID tenantId, FailureClass failureClass) {
        Instant now = Instant.now();
        Optional<SyncIncident> existing = incidentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isEmpty()) {
            SyncIncident incident = SyncIncident.open(UUID.randomUUID(), idempotencyKey, tenantId, failureClass, now);
            SyncIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        } else {
            SyncIncident incident = existing.get();
            incident.recordFailure(now);
            SyncIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        }
    }

    private void publishAndClear(SyncIncident incident) {
        incident.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        incident.clearEvents();
    }
}
