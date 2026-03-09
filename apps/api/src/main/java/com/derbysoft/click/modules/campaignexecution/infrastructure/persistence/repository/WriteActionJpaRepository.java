package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.WriteActionEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WriteActionJpaRepository extends JpaRepository<WriteActionEntity, UUID> {

    Optional<WriteActionEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT a FROM WriteActionEntity a WHERE a.status = 'PENDING' " +
           "AND (a.nextAttemptAfter IS NULL OR a.nextAttemptAfter <= :now) " +
           "ORDER BY a.createdAt ASC")
    List<WriteActionEntity> findPendingActions(@Param("now") Instant now);

    @Query("SELECT a FROM WriteActionEntity a WHERE a.status = 'RUNNING' AND a.leaseExpiresAt < :now")
    List<WriteActionEntity> findRunningActionsWithExpiredLease(@Param("now") Instant now);

    List<WriteActionEntity> findByRevisionId(UUID revisionId);

    long countByTenantIdAndTriggerTypeInAndCreatedAtAfter(
        UUID tenantId, List<String> triggerTypes, Instant createdAt);
}
