package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.domain.ExecutionIncidentRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AcknowledgeEscalationHandler {

    private final ExecutionIncidentRepository incidentRepository;
    private final InProcessEventBus eventBus;

    public AcknowledgeEscalationHandler(ExecutionIncidentRepository incidentRepository,
                                         InProcessEventBus eventBus) {
        this.incidentRepository = incidentRepository;
        this.eventBus = eventBus;
    }

    public ExecutionIncident acknowledge(UUID incidentId, String ackReason, String by) {
        ExecutionIncident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404",
                "ExecutionIncident not found: " + incidentId));

        incident.acknowledge(ackReason, by, Instant.now());
        ExecutionIncident saved = incidentRepository.save(incident);
        saved.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        saved.clearEvents();
        return saved;
    }
}
