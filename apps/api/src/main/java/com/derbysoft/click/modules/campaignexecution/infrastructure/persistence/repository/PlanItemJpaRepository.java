package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.PlanItemEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanItemJpaRepository extends JpaRepository<PlanItemEntity, UUID> {

    List<PlanItemEntity> findByRevisionId(UUID revisionId);

    @Query("SELECT i FROM PlanItemEntity i WHERE i.status = 'QUEUED' " +
           "AND (i.nextAttemptAfter IS NULL OR i.nextAttemptAfter <= :now) " +
           "ORDER BY i.applyOrder ASC")
    List<PlanItemEntity> findQueuedItems(@Param("now") Instant now);

    List<PlanItemEntity> findByRevisionIdAndStatusIn(UUID revisionId, List<String> statuses);
}
