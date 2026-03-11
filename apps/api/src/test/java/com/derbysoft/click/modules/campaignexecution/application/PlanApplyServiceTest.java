package com.derbysoft.click.modules.campaignexecution.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.application.handlers.ManualExecutionRateLimitService;
import com.derbysoft.click.modules.campaignexecution.application.handlers.PlanApplyService;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.AccountBindingInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanApplyServiceTest {

    @Mock PlanRevisionRepository revisionRepository;
    @Mock PlanItemRepository planItemRepository;
    @Mock WriteActionRepository writeActionRepository;
    @Mock GoogleAdsQueryPort googleAdsQueryPort;
    @Mock ManualExecutionRateLimitService rateLimitService;
    @Mock InProcessEventBus eventBus;

    private PlanApplyService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    @BeforeEach
    void setUp() {
        service = new PlanApplyService(revisionRepository, planItemRepository,
            writeActionRepository, googleAdsQueryPort, rateLimitService, eventBus);
    }

    private PlanRevision publishedRevision() {
        PlanRevision r = PlanRevision.create(REVISION_ID, PLAN_ID, TENANT_ID, 1, NOW);
        r.publish("user-1", NOW);
        r.clearEvents();
        return r;
    }

    private PlanItem planItem(UUID revisionId) {
        PlanItem item = PlanItem.create(UUID.randomUUID(), revisionId, TENANT_ID,
            WriteActionType.CREATE_CAMPAIGN, "CAMPAIGN", null,
            "{}", ApplyOrder.CAMPAIGN, NOW);
        item.publish();
        return item;
    }

    @Test
    void shouldEnqueueAllItemsAndCreateWriteActions() {
        PlanRevision revision = publishedRevision();
        PlanItem item = planItem(REVISION_ID);

        when(revisionRepository.findActiveApplyingByTenantId(TENANT_ID)).thenReturn(Optional.empty());
        when(revisionRepository.findById(REVISION_ID)).thenReturn(Optional.of(revision));
        when(googleAdsQueryPort.listActiveBindings(TENANT_ID)).thenReturn(List.of(
            new AccountBindingInfo(UUID.randomUUID(), TENANT_ID, "123456789", "ACTIVE", "CUSTOMER", null, null)));
        when(planItemRepository.findByRevisionId(REVISION_ID)).thenReturn(List.of(item));
        when(revisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(writeActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlanRevision result = service.applyRevision(REVISION_ID, TENANT_ID, "user-1", "apply");

        assertThat(result.getStatus()).isEqualTo(PlanRevisionStatus.APPLYING);
        verify(writeActionRepository).save(any(WriteAction.class));
    }

    @Test
    void shouldRejectWhenRevisionNotPublished() {
        PlanRevision draft = PlanRevision.create(REVISION_ID, PLAN_ID, TENANT_ID, 1, NOW);

        when(revisionRepository.findActiveApplyingByTenantId(TENANT_ID)).thenReturn(Optional.empty());
        when(revisionRepository.findById(REVISION_ID)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.applyRevision(REVISION_ID, TENANT_ID, "user-1", null))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("PUBLISHED");
    }

    @Test
    void shouldRejectWhenAnotherRevisionIsApplying() {
        PlanRevision applying = publishedRevision();
        applying.startApply(NOW);
        when(revisionRepository.findActiveApplyingByTenantId(TENANT_ID))
            .thenReturn(Optional.of(applying));

        assertThatThrownBy(() -> service.applyRevision(REVISION_ID, TENANT_ID, "user-1", null))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("already being applied");
    }
}
