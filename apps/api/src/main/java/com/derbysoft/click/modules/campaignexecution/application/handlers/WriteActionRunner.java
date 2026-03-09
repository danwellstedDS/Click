package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WriteActionRunner {

    private static final Logger log = LoggerFactory.getLogger(WriteActionRunner.class);
    private static final int MAX_ACTIONS_PER_TICK = 10;

    private final WriteActionRepository writeActionRepository;
    private final WriteActionExecutor writeActionExecutor;
    private final RetryPolicyEngine retryPolicyEngine;
    private final ExecutionIncidentLifecycleService incidentLifecycleService;

    public WriteActionRunner(WriteActionRepository writeActionRepository,
                              WriteActionExecutor writeActionExecutor,
                              RetryPolicyEngine retryPolicyEngine,
                              ExecutionIncidentLifecycleService incidentLifecycleService) {
        this.writeActionRepository = writeActionRepository;
        this.writeActionExecutor = writeActionExecutor;
        this.retryPolicyEngine = retryPolicyEngine;
        this.incidentLifecycleService = incidentLifecycleService;
    }

    @Scheduled(fixedDelay = 15_000)
    public void runPendingActions() {
        Instant now = Instant.now();

        // 1. Recover actions with expired leases
        List<WriteAction> expired = writeActionRepository.findRunningActionsWithExpiredLease(now);
        for (WriteAction action : expired) {
            try {
                if (action.canRetry()) {
                    var delay = retryPolicyEngine.computeDelay(action);
                    action.requeueForRetry(now.plus(delay), now);
                    writeActionRepository.save(action);
                } else {
                    action.markFailed(FailureClass.TRANSIENT, "lease expired — max attempts reached", now);
                    writeActionRepository.save(action);
                    incidentLifecycleService.onFailure(
                        action.getRevisionId(), action.getItemId(),
                        action.getTenantId(), FailureClass.TRANSIENT);
                }
            } catch (Exception e) {
                log.warn("Failed to recover expired action {}: {}", action.getId(), e.getMessage());
            }
        }

        // 2. Pick up to 10 PENDING actions ordered by apply_order (via PlanItem.applyOrder)
        List<WriteAction> pending = writeActionRepository.findPendingActions(now);
        int count = 0;
        for (WriteAction action : pending) {
            if (count >= MAX_ACTIONS_PER_TICK) break;
            try {
                writeActionExecutor.execute(action.getId());
                count++;
            } catch (Exception e) {
                log.warn("Failed to execute action {}: {}", action.getId(), e.getMessage());
            }
        }
    }
}
