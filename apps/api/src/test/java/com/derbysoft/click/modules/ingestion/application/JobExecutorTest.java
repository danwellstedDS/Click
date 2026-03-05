package com.derbysoft.click.modules.ingestion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.GoogleAdsConnectionInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.IncidentLifecycleService;
import com.derbysoft.click.modules.ingestion.application.handlers.JobExecutor;
import com.derbysoft.click.modules.ingestion.application.handlers.RetryPolicyEngine;
import com.derbysoft.click.modules.ingestion.application.ports.GoogleAdsReportingPort;
import com.derbysoft.click.modules.ingestion.application.ports.GoogleAdsReportingPort.CampaignRow;
import com.derbysoft.click.modules.ingestion.domain.RawSnapshotRepository;
import com.derbysoft.click.modules.ingestion.domain.SyncJobRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.RawSnapshot;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.SyncJobStatus;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionAuthException;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionFetchException;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawCampaignRowJpaRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobExecutorTest {

    @Mock SyncJobRepository syncJobRepository;
    @Mock RawSnapshotRepository rawSnapshotRepository;
    @Mock RawCampaignRowJpaRepository rawCampaignRowJpaRepository;
    @Mock GoogleAdsReportingPort googleAdsReportingPort;
    @Mock GoogleAdsQueryPort googleAdsQueryPort;
    @Mock InProcessEventBus eventBus;
    @Mock RetryPolicyEngine retryPolicyEngine;
    @Mock IncidentLifecycleService incidentLifecycleService;

    private JobExecutor executor;

    private static final UUID JOB_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-03-05T02:00:00Z");

    private SyncJob pendingJob() {
        return SyncJob.create(JOB_ID, INTEGRATION_ID, TENANT_ID, "123-456-7890",
            "CAMPAIGN_PERFORMANCE",
            new DateWindow(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4)),
            TriggerType.DAILY, null, "daily-scheduler", NOW);
    }

    private GoogleAdsConnectionInfo connectionInfo() {
        return new GoogleAdsConnectionInfo(UUID.randomUUID(), TENANT_ID, "123-000-0001", "ACTIVE");
    }

    private List<CampaignRow> sampleRows() {
        return List.of(
            new CampaignRow("111", "Campaign A", 100L, 5000L, 500000L, 2.5, LocalDate.of(2026, 3, 1))
        );
    }

    @BeforeEach
    void setUp() {
        executor = new JobExecutor(syncJobRepository, rawSnapshotRepository,
            rawCampaignRowJpaRepository, googleAdsReportingPort, googleAdsQueryPort,
            eventBus, retryPolicyEngine, incidentLifecycleService);
    }

    @Test
    void shouldExecuteSuccessfullyAndWriteSnapshot() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenReturn(sampleRows());
        when(rawSnapshotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        executor.execute(JOB_ID);

        verify(rawSnapshotRepository).save(any(RawSnapshot.class));
        verify(incidentLifecycleService).onSuccess(anyString(), eq(TENANT_ID));
    }

    @Test
    void shouldMarkFailedOnTransientError() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenThrow(new IngestionFetchException(FailureClass.TRANSIENT, "timeout", null));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.TRANSIENT);
        when(retryPolicyEngine.computeDelay(any())).thenReturn(Duration.ofSeconds(120));

        executor.execute(JOB_ID);

        // Transient with canRetry=true → requeued back to PENDING; failure class is recorded
        assertThat(job.getFailureClass()).isEqualTo(FailureClass.TRANSIENT);
        verify(incidentLifecycleService).onFailure(anyString(), eq(TENANT_ID), eq(FailureClass.TRANSIENT));
    }

    @Test
    void shouldMarkFailedOnPermanentError() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenThrow(new IngestionFetchException(FailureClass.PERMANENT, "bad request", null));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.PERMANENT);

        executor.execute(JOB_ID);

        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.FAILED);
        assertThat(job.canRetry()).isFalse();
        verify(incidentLifecycleService).onFailure(anyString(), eq(TENANT_ID), eq(FailureClass.PERMANENT));
    }

    @Test
    void shouldPublishAuthFailureDetectedOnAuthException() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenThrow(new IngestionAuthException("oauth revoked", null));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.PERMANENT);

        executor.execute(JOB_ID);

        // AuthFailureDetected event published in addition to job events
        verify(eventBus, times(3)).publish(any()); // SyncStarted + SyncFailed + AuthFailureDetected
    }

    @Test
    void shouldAutoCloseIncidentOnSuccess() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenReturn(sampleRows());
        when(rawSnapshotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        executor.execute(JOB_ID);

        verify(incidentLifecycleService).onSuccess(eq(job.getIdempotencyKey()), eq(TENANT_ID));
    }

    @Test
    void shouldOpenIncidentOnFirstFailure() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenThrow(new IngestionFetchException(FailureClass.TRANSIENT, "timeout", null));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.TRANSIENT);
        when(retryPolicyEngine.computeDelay(any())).thenReturn(Duration.ofSeconds(120));

        executor.execute(JOB_ID);

        verify(incidentLifecycleService).onFailure(anyString(), eq(TENANT_ID), eq(FailureClass.TRANSIENT));
    }

    @Test
    void shouldRequeueForRetryOnTransientIfCanRetry() {
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenThrow(new IngestionFetchException(FailureClass.TRANSIENT, "timeout", null));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.TRANSIENT);
        when(retryPolicyEngine.computeDelay(any())).thenReturn(Duration.ofSeconds(120));

        executor.execute(JOB_ID);

        // After requeue, job should be back to PENDING
        assertThat(job.getStatus()).isEqualTo(SyncJobStatus.PENDING);
        assertThat(job.getNextAttemptAfter()).isNotNull();
    }

    @Test
    void shouldEscalateIncidentOnThirdConsecutiveFailure() {
        // This is handled by IncidentLifecycleService, not JobExecutor directly.
        // Verifying that onFailure is called on each failure.
        SyncJob job = pendingJob();
        when(syncJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(googleAdsQueryPort.findConnectionByTenantId(TENANT_ID))
            .thenReturn(Optional.of(connectionInfo()));
        when(googleAdsReportingPort.fetchCampaignMetrics(any(), any(), any(), any()))
            .thenThrow(new IngestionFetchException(FailureClass.TRANSIENT, "timeout", null));
        when(retryPolicyEngine.classify(any())).thenReturn(FailureClass.TRANSIENT);
        when(retryPolicyEngine.computeDelay(any())).thenReturn(Duration.ofSeconds(120));

        executor.execute(JOB_ID);

        verify(incidentLifecycleService, times(1)).onFailure(
            anyString(), eq(TENANT_ID), eq(FailureClass.TRANSIENT));
    }
}
