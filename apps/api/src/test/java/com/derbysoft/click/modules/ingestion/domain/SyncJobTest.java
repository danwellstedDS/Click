package com.derbysoft.click.modules.ingestion.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.events.SyncFailed;
import com.derbysoft.click.modules.ingestion.domain.events.SyncStarted;
import com.derbysoft.click.modules.ingestion.domain.events.SyncSucceeded;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.SyncJobStatus;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SyncJobTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final DateWindow DATE_WINDOW = new DateWindow(
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4)
    );
    private static final Instant NOW = Instant.parse("2026-03-05T02:00:00Z");

    private SyncJob newJob() {
        return SyncJob.create(ID, INTEGRATION_ID, TENANT_ID, "123-456-7890",
            "CAMPAIGN_PERFORMANCE", DATE_WINDOW, TriggerType.DAILY, null, "daily-scheduler", NOW);
    }

    @Test
    void shouldCreateJobInPendingStatus() {
        SyncJob job = newJob();

        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.PENDING);
        assertThat(job.getAttempts()).isEqualTo(0);
        assertThat(job.getLeaseExpiresAt()).isNull();
        assertThat(job.getFailureClass()).isNull();
    }

    @Test
    void shouldEmitSyncStartedOnCreate() {
        SyncJob job = newJob();

        assertThat(job.getEvents()).hasSize(1);
        assertThat(job.getEvents().get(0)).isInstanceOf(SyncStarted.class);
        SyncStarted event = (SyncStarted) job.getEvents().get(0);
        assertThat(event.jobId()).isEqualTo(ID);
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.triggerType()).isEqualTo(TriggerType.DAILY);
    }

    @Test
    void shouldAcquireLeaseIncrementAttemptsAndSetExpiry() {
        SyncJob job = newJob();
        job.clearEvents();

        job.acquireLease(NOW);

        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.RUNNING);
        assertThat(job.getAttempts()).isEqualTo(1);
        assertThat(job.getLastAttemptAt()).isEqualTo(NOW);
        assertThat(job.getLeaseExpiresAt()).isEqualTo(NOW.plusSeconds(3600));
    }

    @Test
    void shouldMarkSucceededAndEmitEvent() {
        SyncJob job = newJob();
        job.acquireLease(NOW);
        job.clearEvents();

        job.markSucceeded(NOW);

        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.SUCCEEDED);
        assertThat(job.getLeaseExpiresAt()).isNull();
        assertThat(job.getEvents()).hasSize(1);
        assertThat(job.getEvents().get(0)).isInstanceOf(SyncSucceeded.class);
    }

    @Test
    void shouldMarkFailedAndEmitEvent() {
        SyncJob job = newJob();
        job.acquireLease(NOW);
        job.clearEvents();

        job.markFailed(FailureClass.TRANSIENT, "timeout", NOW);

        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.FAILED);
        assertThat(job.getFailureClass()).isEqualTo(FailureClass.TRANSIENT);
        assertThat(job.getFailureReason()).isEqualTo("timeout");
        assertThat(job.getLeaseExpiresAt()).isNull();
        assertThat(job.getEvents()).hasSize(1);
        SyncFailed event = (SyncFailed) job.getEvents().get(0);
        assertThat(event.failureClass()).isEqualTo(FailureClass.TRANSIENT);
    }

    @Test
    void shouldAllowRetryWhenTransientAndUnderLimit() {
        SyncJob job = newJob();
        job.acquireLease(NOW);
        job.markFailed(FailureClass.TRANSIENT, "timeout", NOW);

        assertThat(job.canRetry()).isTrue();
    }

    @Test
    void shouldNotAllowRetryWhenPermanent() {
        SyncJob job = newJob();
        job.acquireLease(NOW);
        job.markFailed(FailureClass.PERMANENT, "auth error", NOW);

        assertThat(job.canRetry()).isFalse();
    }

    @Test
    void shouldNotAllowRetryWhenAttemptsExhausted() {
        SyncJob job = newJob();
        // exhaust all 5 attempts
        for (int i = 0; i < 5; i++) {
            job.acquireLease(NOW);
            job.markFailed(FailureClass.TRANSIENT, "timeout", NOW);
            if (i < 4) {
                job.requeueForRetry(NOW.plusSeconds(60), NOW);
            }
        }

        assertThat(job.getAttempts()).isEqualTo(5);
        assertThat(job.canRetry()).isFalse();
    }
}
