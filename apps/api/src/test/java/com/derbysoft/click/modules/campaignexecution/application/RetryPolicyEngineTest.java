package com.derbysoft.click.modules.campaignexecution.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.campaignexecution.application.handlers.RetryPolicyEngine;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.modules.campaignexecution.infrastructure.googleads.MutationAuthException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetryPolicyEngineTest {

    private final RetryPolicyEngine engine = new RetryPolicyEngine();
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private WriteAction actionWithAttempts(int attempts) {
        WriteAction action = WriteAction.create(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            WriteActionType.CREATE_CAMPAIGN, 0,
            TriggerType.SCHEDULED, "scheduler", "apply", NOW
        );
        for (int i = 0; i < attempts; i++) {
            action.acquireLease(NOW);
            action.markFailed(FailureClass.TRANSIENT, "error", NOW);
            if (i < attempts - 1) action.requeueForRetry(NOW.plusSeconds(60), NOW);
        }
        return action;
    }

    @Test
    void shouldClassifyAuthExceptionAsPermanent() {
        FailureClass fc = engine.classify(new MutationAuthException("auth failed", null));
        assertThat(fc).isEqualTo(FailureClass.PERMANENT);
    }

    @Test
    void shouldClassifyGenericExceptionAsTransient() {
        FailureClass fc = engine.classify(new RuntimeException("network error"));
        assertThat(fc).isEqualTo(FailureClass.TRANSIENT);
    }

    @Test
    void shouldComputeExponentialDelay() {
        WriteAction action = actionWithAttempts(1);
        Duration delay = engine.computeDelay(action);
        // 2^1 * 30 = 60s base, + jitter (0-30s)
        assertThat(delay.getSeconds()).isBetween(60L, 90L);
    }

    @Test
    void shouldCapDelayAt900Seconds() {
        WriteAction action = actionWithAttempts(3);
        Duration delay = engine.computeDelay(action);
        // 2^3 * 30 = 240s base < 900s cap
        assertThat(delay.getSeconds()).isBetween(240L, 270L);
    }
}
