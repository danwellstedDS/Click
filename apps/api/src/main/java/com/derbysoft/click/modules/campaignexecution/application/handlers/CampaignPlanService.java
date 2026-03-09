package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.domain.CampaignPlanRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CampaignPlanService {

    private final CampaignPlanRepository planRepository;
    private final PlanRevisionRepository revisionRepository;
    private final PlanItemRepository planItemRepository;
    private final PublishValidationService publishValidationService;
    private final InProcessEventBus eventBus;

    public CampaignPlanService(CampaignPlanRepository planRepository,
                                PlanRevisionRepository revisionRepository,
                                PlanItemRepository planItemRepository,
                                PublishValidationService publishValidationService,
                                InProcessEventBus eventBus) {
        this.planRepository = planRepository;
        this.revisionRepository = revisionRepository;
        this.planItemRepository = planItemRepository;
        this.publishValidationService = publishValidationService;
        this.eventBus = eventBus;
    }

    public CampaignPlan createPlan(UUID tenantId, String name, String description,
                                    String createdBy) {
        CampaignPlan plan = CampaignPlan.create(UUID.randomUUID(), tenantId, name, description,
            Instant.now());
        return planRepository.save(plan);
    }

    public PlanRevision saveDraftRevision(UUID planId, UUID tenantId,
                                           List<PlanItemDraft> items, String by) {
        CampaignPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404",
                "CampaignPlan not found: " + planId));

        // Cancel any existing DRAFT revision
        List<PlanRevision> existing = revisionRepository.findByPlanId(planId);
        existing.stream()
            .filter(r -> r.getStatus() == PlanRevisionStatus.DRAFT)
            .forEach(r -> {
                r.cancel(by, "Replaced by new draft", Instant.now());
                revisionRepository.save(r);
            });

        int nextNumber = existing.size() + 1;
        Instant now = Instant.now();
        PlanRevision revision = PlanRevision.create(UUID.randomUUID(), planId, tenantId,
            nextNumber, now);
        PlanRevision savedRevision = revisionRepository.save(revision);

        List<PlanItem> planItems = items.stream()
            .map(draft -> PlanItem.create(
                UUID.randomUUID(), savedRevision.getId(), tenantId,
                draft.actionType(), draft.resourceType(), draft.resourceId(),
                draft.payload(), draft.applyOrder(), now
            ))
            .toList();
        planItemRepository.saveAll(planItems);

        return savedRevision;
    }

    public PlanRevision publishRevision(UUID revisionId, UUID tenantId, String publishedBy) {
        PlanRevision revision = revisionRepository.findById(revisionId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404",
                "PlanRevision not found: " + revisionId));

        List<PlanItem> items = planItemRepository.findByRevisionId(revisionId);
        publishValidationService.validate(revision, items, tenantId);

        Instant now = Instant.now();
        revision.publish(publishedBy, now);

        items.forEach(item -> {
            item.publish();
            planItemRepository.save(item);
        });

        PlanRevision saved = revisionRepository.save(revision);
        publishAndClear(saved);
        return saved;
    }

    public PlanRevision cancelRevision(UUID revisionId, String cancelledBy, String reason) {
        PlanRevision revision = revisionRepository.findById(revisionId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404",
                "PlanRevision not found: " + revisionId));

        revision.cancel(cancelledBy, reason, Instant.now());
        PlanRevision saved = revisionRepository.save(revision);
        publishAndClear(saved);
        return saved;
    }

    private void publishAndClear(PlanRevision revision) {
        revision.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        revision.clearEvents();
    }

    public record PlanItemDraft(WriteActionType actionType, String resourceType,
                                 String resourceId, String payload, ApplyOrder applyOrder) {}
}
