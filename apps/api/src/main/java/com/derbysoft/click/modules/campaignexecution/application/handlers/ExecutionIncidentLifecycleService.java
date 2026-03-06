package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.domain.ExecutionIncidentRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionIncidentLifecycleService {

    private final ExecutionIncidentRepository incidentRepository;
    private final InProcessEventBus eventBus;

    public ExecutionIncidentLifecycleService(ExecutionIncidentRepository incidentRepository,
                                              InProcessEventBus eventBus) {
        this.incidentRepository = incidentRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void onSuccess(String idempotencyKey, UUID tenantId) {
        Instant now = Instant.now();
        Optional<ExecutionIncident> existing = incidentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            ExecutionIncident incident = existing.get();
            incident.autoClose(now);
            ExecutionIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        }
    }

    @Transactional
    public void onFailure(String idempotencyKey, UUID tenantId, FailureClass failureClass) {
        Instant now = Instant.now();
        Optional<ExecutionIncident> existing = incidentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isEmpty()) {
            ExecutionIncident incident = ExecutionIncident.open(
                UUID.randomUUID(), idempotencyKey, tenantId, failureClass, now);
            ExecutionIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        } else {
            ExecutionIncident incident = existing.get();
            incident.recordFailure(now);
            ExecutionIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        }
    }

    private void publishAndClear(ExecutionIncident incident) {
        incident.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        incident.clearEvents();
    }
}
