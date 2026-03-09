package com.derbysoft.click.modules.campaignexecution.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.application.handlers.CampaignPlanService;
import com.derbysoft.click.modules.campaignexecution.application.handlers.PublishValidationService;
import com.derbysoft.click.modules.campaignexecution.domain.CampaignPlanRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
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
class CampaignPlanServiceTest {

    @Mock CampaignPlanRepository planRepository;
    @Mock PlanRevisionRepository revisionRepository;
    @Mock PlanItemRepository planItemRepository;
    @Mock PublishValidationService publishValidationService;
    @Mock InProcessEventBus eventBus;

    private CampaignPlanService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    @BeforeEach
    void setUp() {
        service = new CampaignPlanService(planRepository, revisionRepository,
            planItemRepository, publishValidationService, eventBus);
    }

    @Test
    void shouldCreatePlan() {
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CampaignPlan plan = service.createPlan(TENANT_ID, "My Plan", "desc", "user-1");

        assertThat(plan.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(plan.getName()).isEqualTo("My Plan");
        verify(planRepository).save(any());
    }

    @Test
    void shouldPublishRevisionAfterValidation() {
        PlanRevision revision = PlanRevision.create(REVISION_ID, UUID.randomUUID(), TENANT_ID, 1, NOW);
        when(revisionRepository.findById(REVISION_ID)).thenReturn(Optional.of(revision));
        when(planItemRepository.findByRevisionId(REVISION_ID)).thenReturn(List.of());
        when(revisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlanRevision result = service.publishRevision(REVISION_ID, TENANT_ID, "user-1");

        assertThat(result.getStatus()).isEqualTo(PlanRevisionStatus.PUBLISHED);
        verify(publishValidationService).validate(any(), any(), any());
    }

    @Test
    void shouldThrowWhenPublishValidationFails() {
        PlanRevision revision = PlanRevision.create(REVISION_ID, UUID.randomUUID(), TENANT_ID, 1, NOW);
        when(revisionRepository.findById(REVISION_ID)).thenReturn(Optional.of(revision));
        when(planItemRepository.findByRevisionId(REVISION_ID)).thenReturn(List.of());
        doThrow(new DomainError.ValidationError("CE_400", "No connection"))
            .when(publishValidationService).validate(any(), any(), any());

        assertThatThrownBy(() -> service.publishRevision(REVISION_ID, TENANT_ID, "user-1"))
            .isInstanceOf(DomainError.ValidationError.class);
    }

    @Test
    void shouldCancelRevision() {
        PlanRevision revision = PlanRevision.create(REVISION_ID, UUID.randomUUID(), TENANT_ID, 1, NOW);
        when(revisionRepository.findById(REVISION_ID)).thenReturn(Optional.of(revision));
        when(revisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlanRevision result = service.cancelRevision(REVISION_ID, "user-1", "no longer needed");

        assertThat(result.getStatus()).isEqualTo(PlanRevisionStatus.CANCELLED);
    }
}
