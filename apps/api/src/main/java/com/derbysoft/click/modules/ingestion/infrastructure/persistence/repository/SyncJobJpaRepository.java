package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.SyncJobEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyncJobJpaRepository extends JpaRepository<SyncJobEntity, UUID> {

    Optional<SyncJobEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT j FROM SyncJobEntity j WHERE j.status = 'PENDING' " +
           "AND (j.nextAttemptAfter IS NULL OR j.nextAttemptAfter <= :now) " +
           "ORDER BY j.createdAt ASC")
    List<SyncJobEntity> findPendingJobs(@Param("now") Instant now);

    @Query("SELECT j FROM SyncJobEntity j WHERE j.status = 'RUNNING' AND j.leaseExpiresAt < :now")
    List<SyncJobEntity> findRunningJobsWithExpiredLease(@Param("now") Instant now);

    List<SyncJobEntity> findByIntegrationId(UUID integrationId);

    long countByTenantIdAndTriggerTypeInAndCreatedAtAfter(
        UUID tenantId, List<String> triggerTypes, Instant createdAfter);

    @Query("SELECT MIN(j.createdAt) FROM SyncJobEntity j " +
           "WHERE j.tenantId = :tenantId AND j.triggerType IN :triggerTypes AND j.createdAt > :since")
    Instant findOldestManualTriggerCreatedAt(
        @Param("tenantId") UUID tenantId,
        @Param("triggerTypes") List<String> triggerTypes,
        @Param("since") Instant since);
}
