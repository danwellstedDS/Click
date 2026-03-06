package com.derbysoft.click.modules.campaignexecution.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionBlocked;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionFailed;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionQueued;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionStarted;
import com.derbysoft.click.modules.campaignexecution.domain.events.WriteActionSucceeded;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanItemTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private PlanItem newItem() {
        return PlanItem.create(ID, REVISION_ID, TENANT_ID,
            WriteActionType.CREATE_CAMPAIGN, "CAMPAIGN", null,
            "{\"name\":\"Test Campaign\"}", ApplyOrder.CAMPAIGN, NOW);
    }

    @Test
    void shouldCreateItemInDraftStatus() {
        PlanItem item = newItem();

        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.DRAFT);
        assertThat(item.getAttempts()).isEqualTo(0);
        assertThat(item.getMaxAttempts()).isEqualTo(3);
        assertThat(item.getEvents()).isEmpty();
    }

    @Test
    void shouldPublishItem() {
        PlanItem item = newItem();
        item.publish();
        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.PUBLISHED);
    }

    @Test
    void shouldEnqueueAndEmitEvent() {
        PlanItem item = newItem();
        item.publish();

        item.enqueue(NOW);

        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.QUEUED);
        assertThat(item.getEvents()).hasSize(1);
        assertThat(item.getEvents().get(0)).isInstanceOf(WriteActionQueued.class);
    }

    @Test
    void shouldStartExecutionAndIncrementAttempts() {
        PlanItem item = newItem();
        item.publish();
        item.enqueue(NOW);
        item.clearEvents();

        item.startExecution(NOW);

        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.IN_PROGRESS);
        assertThat(item.getAttempts()).isEqualTo(1);
        assertThat(item.getEvents()).hasSize(1);
        assertThat(item.getEvents().get(0)).isInstanceOf(WriteActionStarted.class);
    }

    @Test
    void shouldMarkSucceededAndEmitEvent() {
        PlanItem item = newItem();
        item.publish();
        item.enqueue(NOW);
        item.startExecution(NOW);
        item.clearEvents();

        item.markSucceeded("campaigns/123", NOW);

        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.SUCCEEDED);
        assertThat(item.getResourceId()).isEqualTo("campaigns/123");
        assertThat(item.getEvents()).hasSize(1);
        assertThat(item.getEvents().get(0)).isInstanceOf(WriteActionSucceeded.class);
    }

    @Test
    void shouldMarkFailedAndEmitEvent() {
        PlanItem item = newItem();
        item.publish();
        item.enqueue(NOW);
        item.startExecution(NOW);
        item.clearEvents();

        item.markFailed(FailureClass.TRANSIENT, "network timeout", NOW);

        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.FAILED);
        assertThat(item.getFailureClass()).isEqualTo(FailureClass.TRANSIENT);
        assertThat(item.getEvents()).hasSize(1);
        assertThat(item.getEvents().get(0)).isInstanceOf(WriteActionFailed.class);
    }

    @Test
    void shouldBlockAndEmitEvent() {
        PlanItem item = newItem();
        item.publish();
        item.enqueue(NOW);
        item.startExecution(NOW);
        item.clearEvents();

        item.block("permanent auth failure", NOW);

        assertThat(item.getStatus()).isEqualTo(PlanItemStatus.BLOCKED);
        assertThat(item.getEvents()).hasSize(1);
        assertThat(item.getEvents().get(0)).isInstanceOf(WriteActionBlocked.class);
    }

    @Test
    void shouldAllowRetryWhenTransientAndUnderLimit() {
        PlanItem item = newItem();
        item.publish();
        item.enqueue(NOW);
        item.startExecution(NOW);
        item.markFailed(FailureClass.TRANSIENT, "timeout", NOW);

        assertThat(item.canRetry()).isTrue();
    }

    @Test
    void shouldNotAllowRetryWhenPermanent() {
        PlanItem item = newItem();
        item.publish();
        item.enqueue(NOW);
        item.startExecution(NOW);
        item.markFailed(FailureClass.PERMANENT, "auth error", NOW);

        assertThat(item.canRetry()).isFalse();
    }

    @Test
    void shouldNotAllowRetryWhenAttemptsExhausted() {
        PlanItem item = newItem();
        for (int i = 0; i < 3; i++) {
            item.publish();
            item.enqueue(NOW);
            item.startExecution(NOW);
            item.markFailed(FailureClass.TRANSIENT, "timeout", NOW);
            if (i < 2) item.requeueForRetry(NOW.plusSeconds(60), NOW);
        }

        assertThat(item.getAttempts()).isEqualTo(3);
        assertThat(item.canRetry()).isFalse();
    }
}
