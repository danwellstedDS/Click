package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ForceRunPlanItemHandler {

    private final PlanItemRepository planItemRepository;
    private final WriteActionRepository writeActionRepository;
    private final ManualExecutionRateLimitService rateLimitService;
    private final GoogleAdsQueryPort googleAdsQueryPort;

    public ForceRunPlanItemHandler(PlanItemRepository planItemRepository,
                                    WriteActionRepository writeActionRepository,
                                    ManualExecutionRateLimitService rateLimitService,
                                    GoogleAdsQueryPort googleAdsQueryPort) {
        this.planItemRepository = planItemRepository;
        this.writeActionRepository = writeActionRepository;
        this.rateLimitService = rateLimitService;
        this.googleAdsQueryPort = googleAdsQueryPort;
    }

    public PlanItem forceRun(UUID itemId, UUID tenantId, String reason, String triggeredBy) {
        PlanItem item = planItemRepository.findById(itemId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404", "PlanItem not found: " + itemId));

        if (item.getStatus() != PlanItemStatus.FAILED && item.getStatus() != PlanItemStatus.BLOCKED) {
            throw new DomainError.Conflict("CE_409",
                "Can only force-run FAILED or BLOCKED items; current status: " + item.getStatus());
        }

        rateLimitService.checkOrThrow(tenantId);

        String targetCustomerId = googleAdsQueryPort.listActiveBindings(tenantId)
            .stream().findFirst()
            .map(b -> b.customerId())
            .orElse(null);

        Instant now = Instant.now();
        item.enqueue(now);
        planItemRepository.save(item);

        WriteAction action = WriteAction.create(
            UUID.randomUUID(), item.getRevisionId(), itemId, tenantId,
            item.getActionType(), item.getAttempts(), targetCustomerId,
            TriggerType.FORCE_RUN, triggeredBy, reason, now
        );
        writeActionRepository.save(action);

        return item;
    }
}
