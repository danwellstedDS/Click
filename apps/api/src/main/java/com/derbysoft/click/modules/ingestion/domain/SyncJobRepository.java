package com.derbysoft.click.modules.ingestion.domain;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SyncJobRepository {
    Optional<SyncJob> findById(UUID id);
    Optional<SyncJob> findByIdempotencyKey(String key);
    List<SyncJob> findPendingJobs(Instant maxNextAttemptAfter);
    List<SyncJob> findRunningJobsWithExpiredLease(Instant now);
    List<SyncJob> findByIntegrationId(UUID integrationId);
    SyncJob save(SyncJob job);
}
