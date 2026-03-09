package com.derbysoft.click.modules.campaignexecution.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.IdempotencyKey;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionStatus;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WriteActionTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private WriteAction newAction() {
        return WriteAction.create(ID, REVISION_ID, ITEM_ID, TENANT_ID,
            WriteActionType.CREATE_CAMPAIGN, 0, "123456789",
            TriggerType.SCHEDULED, "scheduler", "initial apply", NOW);
    }

    @Test
    void shouldComputeIdempotencyKeyOnCreate() {
        WriteAction action = newAction();
        String expected = IdempotencyKey.compute(REVISION_ID, ITEM_ID,
            WriteActionType.CREATE_CAMPAIGN, 0);
        assertThat(action.getIdempotencyKey()).isEqualTo(expected);
    }

    @Test
    void shouldCreateActionInPendingStatus() {
        WriteAction action = newAction();

        assertThat(action.getStatus()).isEqualTo(WriteActionStatus.PENDING);
        assertThat(action.getAttempts()).isEqualTo(0);
        assertThat(action.getLeaseExpiresAt()).isNull();
    }

    @Test
    void shouldAcquireLeaseAndIncrementAttempts() {
        WriteAction action = newAction();

        action.acquireLease(NOW);

        assertThat(action.getStatus()).isEqualTo(WriteActionStatus.RUNNING);
        assertThat(action.getAttempts()).isEqualTo(1);
        assertThat(action.getLastAttemptAt()).isEqualTo(NOW);
        assertThat(action.getLeaseExpiresAt()).isEqualTo(NOW.plusSeconds(600));
    }

    @Test
    void shouldMarkSucceededAndClearLease() {
        WriteAction action = newAction();
        action.acquireLease(NOW);

        action.markSucceeded(NOW);

        assertThat(action.getStatus()).isEqualTo(WriteActionStatus.SUCCEEDED);
        assertThat(action.getLeaseExpiresAt()).isNull();
    }

    @Test
    void shouldMarkFailedWithClassAndReason() {
        WriteAction action = newAction();
        action.acquireLease(NOW);

        action.markFailed(FailureClass.TRANSIENT, "network error", NOW);

        assertThat(action.getStatus()).isEqualTo(WriteActionStatus.FAILED);
        assertThat(action.getFailureClass()).isEqualTo(FailureClass.TRANSIENT);
        assertThat(action.getFailureReason()).isEqualTo("network error");
    }

    @Test
    void shouldAllowRetryWhenTransientAndUnderLimit() {
        WriteAction action = newAction();
        action.acquireLease(NOW);
        action.markFailed(FailureClass.TRANSIENT, "timeout", NOW);

        assertThat(action.canRetry()).isTrue();
    }

    @Test
    void shouldNotAllowRetryWhenPermanent() {
        WriteAction action = newAction();
        action.acquireLease(NOW);
        action.markFailed(FailureClass.PERMANENT, "auth failure", NOW);

        assertThat(action.canRetry()).isFalse();
    }
}
