package com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository;

import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanonicalFactJpaRepository extends JpaRepository<CanonicalFactEntity, UUID> {

    List<CanonicalFactEntity> findByCanonicalBatchId(UUID canonicalBatchId, Pageable pageable);

    long countByCanonicalBatchId(UUID canonicalBatchId);

    long countByCanonicalBatchIdAndQuarantinedTrue(UUID canonicalBatchId);
}
