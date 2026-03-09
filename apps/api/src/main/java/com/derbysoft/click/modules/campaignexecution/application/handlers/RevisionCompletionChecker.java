package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionSummaryUpdated;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevisionCompletionChecker {

    private static final List<PlanItemStatus> TERMINAL_STATUSES = List.of(
        PlanItemStatus.SUCCEEDED, PlanItemStatus.FAILED,
        PlanItemStatus.CANCELLED, PlanItemStatus.BLOCKED
    );

    private final PlanRevisionRepository revisionRepository;
    private final PlanItemRepository planItemRepository;
    private final InProcessEventBus eventBus;

    public RevisionCompletionChecker(PlanRevisionRepository revisionRepository,
                                      PlanItemRepository planItemRepository,
                                      InProcessEventBus eventBus) {
        this.revisionRepository = revisionRepository;
        this.planItemRepository = planItemRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void checkRevisionCompletion(UUID revisionId) {
        PlanRevision revision = revisionRepository.findById(revisionId).orElse(null);
        if (revision == null) return;

        List<PlanItem> items = planItemRepository.findByRevisionId(revisionId);
        if (items.isEmpty()) return;

        boolean allTerminal = items.stream()
            .allMatch(i -> TERMINAL_STATUSES.contains(i.getStatus()));
        if (!allTerminal) return;

        long succeeded = items.stream()
            .filter(i -> i.getStatus() == PlanItemStatus.SUCCEEDED).count();
        long failed = items.stream()
            .filter(i -> i.getStatus() == PlanItemStatus.FAILED
                      || i.getStatus() == PlanItemStatus.BLOCKED).count();

        Instant now = Instant.now();
        revision.completeApply((int) succeeded, (int) failed, now);
        PlanRevision saved = revisionRepository.save(revision);
        saved.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        saved.clearEvents();

        // Gap #10: emit ExecutionSummaryUpdated after completion
        eventBus.publish(EventEnvelope.of("ExecutionSummaryUpdated",
            new ExecutionSummaryUpdated(
                revision.getId(),
                revision.getTenantId(),
                0, 0,  // queued/inProgress are 0 at completion
                (int) succeeded,
                (int) failed,
                0,     // blocked count already included in failed per spec
                now
            )
        ));
    }
}
