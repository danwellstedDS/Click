package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.modules.ingestion.domain.SyncJobRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);
    private static final int MAX_JOBS_PER_TICK = 5;

    private final SyncJobRepository syncJobRepository;
    private final JobExecutor jobExecutor;
    private final RetryPolicyEngine retryPolicyEngine;
    private final IncidentLifecycleService incidentLifecycleService;

    public JobRunner(
        SyncJobRepository syncJobRepository,
        JobExecutor jobExecutor,
        RetryPolicyEngine retryPolicyEngine,
        IncidentLifecycleService incidentLifecycleService
    ) {
        this.syncJobRepository = syncJobRepository;
        this.jobExecutor = jobExecutor;
        this.retryPolicyEngine = retryPolicyEngine;
        this.incidentLifecycleService = incidentLifecycleService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void runPendingJobs() {
        Instant now = Instant.now();

        // 1. Recover stuck jobs: RUNNING with expired lease
        List<SyncJob> expiredJobs = syncJobRepository.findRunningJobsWithExpiredLease(now);
        for (SyncJob job : expiredJobs) {
            try {
                if (job.canRetry()) {
                    var delay = retryPolicyEngine.computeDelay(job);
                    job.requeueForRetry(now.plus(delay), now);
                    syncJobRepository.save(job);
                } else {
                    job.markStuck(now);
                    syncJobRepository.save(job);
                    incidentLifecycleService.onFailure(
                        job.getIdempotencyKey(), job.getTenantId(), FailureClass.TRANSIENT);
                }
            } catch (Exception e) {
                log.warn("Failed to recover stuck job {}: {}", job.getId(), e.getMessage());
            }
        }

        // 2. Pick up to 5 PENDING jobs where nextAttemptAfter <= now
        List<SyncJob> pendingJobs = syncJobRepository.findPendingJobs(now);
        int count = 0;
        for (SyncJob job : pendingJobs) {
            if (count >= MAX_JOBS_PER_TICK) break;
            try {
                jobExecutor.execute(job.getId());
                count++;
            } catch (Exception e) {
                log.warn("Failed to execute job {}: {}", job.getId(), e.getMessage());
            }
        }
    }
}
