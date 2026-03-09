package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.AccountBindingInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanApplyService {

    private final PlanRevisionRepository revisionRepository;
    private final PlanItemRepository planItemRepository;
    private final WriteActionRepository writeActionRepository;
    private final GoogleAdsQueryPort googleAdsQueryPort;
    private final ManualExecutionRateLimitService rateLimitService;
    private final InProcessEventBus eventBus;

    public PlanApplyService(PlanRevisionRepository revisionRepository,
                             PlanItemRepository planItemRepository,
                             WriteActionRepository writeActionRepository,
                             GoogleAdsQueryPort googleAdsQueryPort,
                             ManualExecutionRateLimitService rateLimitService,
                             InProcessEventBus eventBus) {
        this.revisionRepository = revisionRepository;
        this.planItemRepository = planItemRepository;
        this.writeActionRepository = writeActionRepository;
        this.googleAdsQueryPort = googleAdsQueryPort;
        this.rateLimitService = rateLimitService;
        this.eventBus = eventBus;
    }

    public PlanRevision applyRevision(UUID revisionId, UUID tenantId,
                                       String triggeredBy, String reason) {
        // Gap #6: count APPLY as a manual trigger against the rate limit
        rateLimitService.checkOrThrow(tenantId);

        if (revisionRepository.findActiveApplyingByTenantId(tenantId).isPresent()) {
            throw new DomainError.Conflict("CE_409",
                "Another revision is already being applied for tenant: " + tenantId);
        }

        PlanRevision revision = revisionRepository.findById(revisionId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404",
                "PlanRevision not found: " + revisionId));

        if (revision.getStatus() != PlanRevisionStatus.PUBLISHED) {
            throw new DomainError.Conflict("CE_409",
                "Can only apply PUBLISHED revisions; current status: " + revision.getStatus());
        }

        // Gap #2: require an active binding — fail fast if none found
        List<AccountBindingInfo> bindings = googleAdsQueryPort.listActiveBindings(tenantId);
        if (bindings.isEmpty()) {
            throw new DomainError.Conflict("CE_409",
                "No active account binding found for tenant: " + tenantId +
                ". Ensure a Google Ads account binding is active before applying.");
        }

        // Use the first active binding (one connection per tenant)
        String targetCustomerId = bindings.get(0).customerId();

        Instant now = Instant.now();
        revision.startApply(now);
        PlanRevision saved = revisionRepository.save(revision);
        publishAndClear(saved);

        List<PlanItem> items = planItemRepository.findByRevisionId(revisionId);
        for (PlanItem item : items) {
            item.enqueue(now);
            planItemRepository.save(item);
            publishAndClear(item);

            WriteAction action = WriteAction.create(
                UUID.randomUUID(), revisionId, item.getId(), tenantId,
                item.getActionType(), item.getAttempts(),
                targetCustomerId,
                TriggerType.APPLY, triggeredBy, reason, now
            );
            writeActionRepository.save(action);
        }

        return saved;
    }

    private void publishAndClear(PlanRevision revision) {
        revision.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        revision.clearEvents();
    }

    private void publishAndClear(PlanItem item) {
        item.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        item.clearEvents();
    }
}
