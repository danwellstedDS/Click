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
    public void onSuccess(UUID revisionId, UUID itemId, UUID tenantId) {
        Instant now = Instant.now();
        Optional<ExecutionIncident> existing =
            incidentRepository.findByRevisionIdAndItemId(revisionId, itemId);
        if (existing.isPresent()) {
            ExecutionIncident incident = existing.get();
            incident.autoClose(now);
            ExecutionIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        }
    }

    @Transactional
    public void onFailure(UUID revisionId, UUID itemId, UUID tenantId, FailureClass failureClass) {
        Instant now = Instant.now();
        String failureClassKey = failureClass.name();
        Optional<ExecutionIncident> existing =
            incidentRepository.findByRevisionIdAndItemIdAndFailureClass(
                revisionId, itemId, failureClassKey);

        if (existing.isEmpty()) {
            ExecutionIncident incident = ExecutionIncident.open(
                UUID.randomUUID(), revisionId, itemId, failureClassKey, tenantId, failureClass, now);
            ExecutionIncident saved = incidentRepository.save(incident);
            publishAndClear(saved);
        } else {
            ExecutionIncident incident = existing.get();
            if (incident.isRecurrenceWindowExpired(now)) {
                // Past 24h window — open a fresh incident
                ExecutionIncident fresh = ExecutionIncident.open(
                    UUID.randomUUID(), revisionId, itemId, failureClassKey, tenantId, failureClass, now);
                ExecutionIncident saved = incidentRepository.save(fresh);
                publishAndClear(saved);
            } else {
                // Reopen or continue existing incident
                incident.recordFailure(now);
                ExecutionIncident saved = incidentRepository.save(incident);
                publishAndClear(saved);
            }
        }
    }

    private void publishAndClear(ExecutionIncident incident) {
        incident.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        incident.clearEvents();
    }
}
