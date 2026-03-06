package com.derbysoft.click.modules.campaignexecution.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionApplyCompleted;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionApplyStarted;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionCancelled;
import com.derbysoft.click.modules.campaignexecution.domain.events.PlanRevisionPublished;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanRevisionStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanRevisionTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private PlanRevision newRevision() {
        return PlanRevision.create(ID, PLAN_ID, TENANT_ID, 1, NOW);
    }

    @Test
    void shouldCreateRevisionInDraftStatus() {
        PlanRevision revision = newRevision();

        assertThat(revision.getStatus()).isEqualTo(PlanRevisionStatus.DRAFT);
        assertThat(revision.getPublishedBy()).isNull();
        assertThat(revision.getEvents()).isEmpty();
    }

    @Test
    void shouldPublishDraftRevisionAndEmitEvent() {
        PlanRevision revision = newRevision();

        revision.publish("user-1", NOW);

        assertThat(revision.getStatus()).isEqualTo(PlanRevisionStatus.PUBLISHED);
        assertThat(revision.getPublishedBy()).isEqualTo("user-1");
        assertThat(revision.getPublishedAt()).isEqualTo(NOW);
        assertThat(revision.getEvents()).hasSize(1);
        assertThat(revision.getEvents().get(0)).isInstanceOf(PlanRevisionPublished.class);
    }

    @Test
    void shouldRejectPublishingNonDraftRevision() {
        PlanRevision revision = newRevision();
        revision.publish("user-1", NOW);

        assertThatThrownBy(() -> revision.publish("user-2", NOW))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("DRAFT");
    }

    @Test
    void shouldStartApplyFromPublishedAndEmitEvent() {
        PlanRevision revision = newRevision();
        revision.publish("user-1", NOW);
        revision.clearEvents();

        revision.startApply(NOW);

        assertThat(revision.getStatus()).isEqualTo(PlanRevisionStatus.APPLYING);
        assertThat(revision.getEvents()).hasSize(1);
        assertThat(revision.getEvents().get(0)).isInstanceOf(PlanRevisionApplyStarted.class);
    }

    @Test
    void shouldCompleteApplyWithSucceededStatus() {
        PlanRevision revision = newRevision();
        revision.publish("user-1", NOW);
        revision.startApply(NOW);
        revision.clearEvents();

        revision.completeApply(3, 0, NOW);

        assertThat(revision.getStatus()).isEqualTo(PlanRevisionStatus.APPLIED);
        assertThat(revision.getEvents()).hasSize(1);
        PlanRevisionApplyCompleted event = (PlanRevisionApplyCompleted) revision.getEvents().get(0);
        assertThat(event.succeededCount()).isEqualTo(3);
        assertThat(event.failedCount()).isEqualTo(0);
    }

    @Test
    void shouldCompleteApplyWithFailedStatusWhenAllFailed() {
        PlanRevision revision = newRevision();
        revision.publish("user-1", NOW);
        revision.startApply(NOW);
        revision.clearEvents();

        revision.completeApply(0, 2, NOW);

        assertThat(revision.getStatus()).isEqualTo(PlanRevisionStatus.FAILED);
    }

    @Test
    void shouldCancelDraftRevisionAndEmitEvent() {
        PlanRevision revision = newRevision();

        revision.cancel("user-1", "no longer needed", NOW);

        assertThat(revision.getStatus()).isEqualTo(PlanRevisionStatus.CANCELLED);
        assertThat(revision.getCancelledBy()).isEqualTo("user-1");
        assertThat(revision.getCancelReason()).isEqualTo("no longer needed");
        assertThat(revision.getEvents()).hasSize(1);
        assertThat(revision.getEvents().get(0)).isInstanceOf(PlanRevisionCancelled.class);
    }

    @Test
    void shouldRejectCancellingApplyingRevision() {
        PlanRevision revision = newRevision();
        revision.publish("user-1", NOW);
        revision.startApply(NOW);

        assertThatThrownBy(() -> revision.cancel("user-1", "reason", NOW))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("APPLYING");
    }
}
