package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort;
import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort.MutationResult;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionStatus;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WriteActionExecutor {

    private static final Logger log = LoggerFactory.getLogger(WriteActionExecutor.class);

    private final WriteActionRepository writeActionRepository;
    private final PlanItemRepository planItemRepository;
    private final GoogleAdsMutationPort mutationPort;
    private final GoogleAdsQueryPort googleAdsQueryPort;
    private final InProcessEventBus eventBus;
    private final RetryPolicyEngine retryPolicyEngine;
    private final ExecutionIncidentLifecycleService incidentLifecycleService;
    private final RevisionCompletionChecker revisionCompletionChecker;

    public WriteActionExecutor(WriteActionRepository writeActionRepository,
                                PlanItemRepository planItemRepository,
                                GoogleAdsMutationPort mutationPort,
                                GoogleAdsQueryPort googleAdsQueryPort,
                                InProcessEventBus eventBus,
                                RetryPolicyEngine retryPolicyEngine,
                                ExecutionIncidentLifecycleService incidentLifecycleService,
                                RevisionCompletionChecker revisionCompletionChecker) {
        this.writeActionRepository = writeActionRepository;
        this.planItemRepository = planItemRepository;
        this.mutationPort = mutationPort;
        this.googleAdsQueryPort = googleAdsQueryPort;
        this.eventBus = eventBus;
        this.retryPolicyEngine = retryPolicyEngine;
        this.incidentLifecycleService = incidentLifecycleService;
        this.revisionCompletionChecker = revisionCompletionChecker;
    }

    @Transactional
    public void execute(UUID writeActionId) {
        WriteAction action = writeActionRepository.findById(writeActionId)
            .orElseThrow(() -> new IllegalStateException("WriteAction not found: " + writeActionId));

        if (action.getStatus() != WriteActionStatus.PENDING) {
            log.debug("Skipping action {} — status is {}", writeActionId, action.getStatus());
            return;
        }

        PlanItem item = planItemRepository.findById(action.getItemId())
            .orElseThrow(() -> new IllegalStateException("PlanItem not found: " + action.getItemId()));

        Instant now = Instant.now();
        action.acquireLease(now);
        item.startExecution(now);
        writeActionRepository.save(action);
        planItemRepository.save(item);
        publishAndClear(item);

        try {
            var connectionInfo = googleAdsQueryPort.findConnectionByTenantId(action.getTenantId())
                .orElseThrow(() -> new IllegalStateException(
                    "No Google Ads connection for tenant: " + action.getTenantId()));

            String customerId = resolveCustomerId(action);
            String managerId = connectionInfo.managerId();

            MutationResult result = dispatch(action, item, customerId, managerId);

            now = Instant.now();
            action.markSucceeded(now);
            item.markSucceeded(result.resourceId(), now);
            writeActionRepository.save(action);
            planItemRepository.save(item);
            publishAndClear(item);

            incidentLifecycleService.onSuccess(action.getIdempotencyKey(), action.getTenantId());
            revisionCompletionChecker.checkRevisionCompletion(action.getRevisionId());

        } catch (Exception e) {
            FailureClass fc = retryPolicyEngine.classify(e);
            now = Instant.now();
            action.markFailed(fc, e.getMessage(), now);
            writeActionRepository.save(action);

            incidentLifecycleService.onFailure(action.getIdempotencyKey(), action.getTenantId(), fc);

            if (action.canRetry()) {
                var delay = retryPolicyEngine.computeDelay(action);
                action.requeueForRetry(now.plus(delay), now);
                item.requeueForRetry(now.plus(delay), now);
                writeActionRepository.save(action);
                planItemRepository.save(item);
            } else {
                if (fc == FailureClass.PERMANENT) {
                    item.block("Permanent failure: " + e.getMessage(), now);
                } else {
                    item.markFailed(fc, e.getMessage(), now);
                }
                planItemRepository.save(item);
                publishAndClear(item);
                revisionCompletionChecker.checkRevisionCompletion(action.getRevisionId());
            }
        }
    }

    private MutationResult dispatch(WriteAction action, PlanItem item,
                                     String customerId, String managerId) {
        String payload = item.getPayload();
        String resourceId = item.getResourceId();
        return switch (action.getActionType()) {
            case CREATE_CAMPAIGN -> mutationPort.createCampaign(customerId, managerId,
                new GoogleAdsMutationPort.CampaignSpec(null, payload));
            case UPDATE_CAMPAIGN -> mutationPort.updateCampaign(customerId, managerId,
                new GoogleAdsMutationPort.CampaignSpec(resourceId, payload));
            case CREATE_AD_GROUP -> mutationPort.createAdGroup(customerId, managerId,
                new GoogleAdsMutationPort.AdGroupSpec(null, resourceId, payload));
            case UPDATE_AD_GROUP -> mutationPort.updateAdGroup(customerId, managerId,
                new GoogleAdsMutationPort.AdGroupSpec(resourceId, null, payload));
            case CREATE_AD -> mutationPort.createAd(customerId, managerId,
                new GoogleAdsMutationPort.AdSpec(null, resourceId, payload));
            case UPDATE_AD -> mutationPort.updateAd(customerId, managerId,
                new GoogleAdsMutationPort.AdSpec(resourceId, null, payload));
            case CREATE_KEYWORD -> mutationPort.createKeyword(customerId, managerId,
                new GoogleAdsMutationPort.KeywordSpec(null, resourceId, payload));
            case UPDATE_KEYWORD -> mutationPort.updateKeyword(customerId, managerId,
                new GoogleAdsMutationPort.KeywordSpec(resourceId, null, payload));
        };
    }

    private String resolveCustomerId(WriteAction action) {
        return action.getTargetCustomerId();
    }

    private void publishAndClear(PlanItem item) {
        item.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        item.clearEvents();
    }
}
