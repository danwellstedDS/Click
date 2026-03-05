package com.derbysoft.click.modules.ingestion.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.ingestion.application.handlers.RetryPolicyEngine;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionAuthException;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionFetchException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetryPolicyEngineTest {

    private final RetryPolicyEngine engine = new RetryPolicyEngine();

    private static final Instant NOW = Instant.parse("2026-03-05T02:00:00Z");

    private SyncJob newJob(int attempts) {
        SyncJob job = SyncJob.create(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "123-456-7890", "CAMPAIGN_PERFORMANCE",
            new DateWindow(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4)),
            TriggerType.DAILY, null, "daily-scheduler", NOW
        );
        for (int i = 0; i < attempts; i++) {
            job.acquireLease(NOW);
            job.markFailed(FailureClass.TRANSIENT, "timeout", NOW);
            if (i < attempts - 1) {
                job.requeueForRetry(NOW.plusSeconds(60), NOW);
            }
        }
        return job;
    }

    @Test
    void shouldClassifyAuthExceptionAsPermanent() {
        FailureClass result = engine.classify(new IngestionAuthException("oauth failure", null));

        assertThat(result).isEqualTo(FailureClass.PERMANENT);
    }

    @Test
    void shouldClassifyTimeoutAsTransient() {
        FailureClass result = engine.classify(
            new IngestionFetchException(FailureClass.TRANSIENT, "deadline exceeded", null));

        assertThat(result).isEqualTo(FailureClass.TRANSIENT);
    }

    @Test
    void shouldClassify429AsTransient() {
        FailureClass result = engine.classify(
            new IngestionFetchException(FailureClass.TRANSIENT, "RESOURCE_EXHAUSTED", null));

        assertThat(result).isEqualTo(FailureClass.TRANSIENT);
    }

    @Test
    void shouldComputeExponentialDelayWithJitter() {
        // After 1 attempt: 2^1 * 60 = 120s, plus jitter 0-30
        SyncJob job = newJob(1);

        Duration delay = engine.computeDelay(job);

        assertThat(delay.getSeconds()).isBetween(120L, 150L);
    }
}
