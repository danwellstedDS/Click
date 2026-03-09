package com.derbysoft.click.modules.campaignexecution.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.application.handlers.ExecutionIncidentLifecycleService;
import com.derbysoft.click.modules.campaignexecution.application.handlers.RetryPolicyEngine;
import com.derbysoft.click.modules.campaignexecution.application.handlers.RevisionCompletionChecker;
import com.derbysoft.click.modules.campaignexecution.application.handlers.WriteActionExecutor;
import com.derbysoft.click.modules.campaignexecution.application.ports.GoogleAdsMutationPort;
import com.derbysoft.click.modules.tenantgovernance.api.ports.TenantGovernancePort;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.modules.campaignexecution.infrastructure.googleads.MutationAuthException;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.GoogleAdsConnectionInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WriteActionExecutorTest {

    @Mock WriteActionRepository writeActionRepository;
    @Mock PlanItemRepository planItemRepository;
    @Mock GoogleAdsMutationPort mutationPort;
    @Mock GoogleAdsQueryPort googleAdsQueryPort;
    @Mock TenantGovernancePort governancePort;
    @Mock InProcessEventBus eventBus;
    @Mock RetryPolicyEngine retryPolicyEngine;
    @Mock ExecutionIncidentLifecycleService incidentLifecycleService;
    @Mock RevisionCompletionChecker revisionCompletionChecker;

    private WriteActionExecutor executor;

    private static final UUID ACTION_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    @BeforeEach
    void setUp() {
        executor = new WriteActionExecutor(writeActionRepository, planItemRepository,
            mutationPort, googleAdsQueryPort, governancePort, eventBus, retryPolicyEngine,
            incidentLifecycleService, revisionCompletionChecker);
    }

    private WriteAction pendingAction() {
        return WriteAction.create(ACTION_ID, REVISION_ID, ITEM_ID, TENANT_ID,
            WriteActionType.CREATE_CAMPAIGN, 0, "123456789",
            TriggerType.SCHEDULED, "scheduler", "apply", NOW);
    }

    private PlanItem publishedItem() {
        PlanItem item = PlanItem.create(ITEM_ID, REVISION_ID, TENANT_ID,
            WriteActionType.CREATE_CAMPAIGN, "CAMPAIGN", null,
            "{\"name\":\"Test\"}", ApplyOrder.CAMPAIGN, NOW);
        item.publish();
        item.enqueue(NOW);
        return item;
    }

    private GoogleAdsConnectionInfo connectionInfo() {
        return new GoogleAdsConnectionInfo(UUID.randomUUID(), TENANT_ID, "123-000-0001", "ACTIVE");
    }

    @Test
    void shouldExecuteSuccessfullyAndMarkItemSucceeded() {
        WriteAction action = pendingAction();
        PlanItem item = publishedItem();

        when(writeActionRepository.findById(ACTION_ID)).thenReturn(Optional.of(action));
        when(planItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(writeActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(mutationPort.createCampaign(any(), any(), any()))
            .thenReturn(new GoogleAdsMutationPort.MutationResult(true, "campaigns/123", null, null));

        executor.execute(ACTION_ID);

        assertThat(action.getStatus()).isEqualTo(WriteActionStatus.SUCCEEDED);
        verify(incidentLifecycleService).onSuccess(eq(REVISION_ID), eq(ITEM_ID), eq(TENANT_ID));
        verify(revisionCompletionChecker).checkRevisionCompletion(REVISION_ID);
    }

    @Test
    void shouldMarkFailedOnTransientErrorAndRequeue() {
        WriteAction action = pendingAction();
        PlanItem item = publishedItem();

        when(writeActionRepository.findById(ACTION_ID)).thenReturn(Optional.of(action));
        when(planItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(writeActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(mutationPort.createCampaign(any(), any(), any()))
            .thenThrow(new RuntimeException("network timeout"));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.TRANSIENT);
        when(retryPolicyEngine.computeDelay(any())).thenReturn(Duration.ofSeconds(60));

        executor.execute(ACTION_ID);

        assertThat(action.getFailureClass()).isEqualTo(FailureClass.TRANSIENT);
        verify(incidentLifecycleService).onFailure(eq(REVISION_ID), eq(ITEM_ID), eq(TENANT_ID), eq(FailureClass.TRANSIENT));
    }

    @Test
    void shouldBlockItemOnPermanentError() {
        WriteAction action = pendingAction();
        PlanItem item = publishedItem();

        when(writeActionRepository.findById(ACTION_ID)).thenReturn(Optional.of(action));
        when(planItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(writeActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(mutationPort.createCampaign(any(), any(), any()))
            .thenThrow(new MutationAuthException("auth revoked", null));

        executor.execute(ACTION_ID);

        verify(incidentLifecycleService).onFailure(eq(REVISION_ID), eq(ITEM_ID), eq(TENANT_ID), eq(FailureClass.PERMANENT));
        verify(revisionCompletionChecker).checkRevisionCompletion(REVISION_ID);
    }
}
