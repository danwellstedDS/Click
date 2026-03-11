package com.derbysoft.click.modules.normalisation.domain;

import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface CanonicalFactRepository {
    void saveAll(List<CanonicalFactEntity> facts);
    List<CanonicalFactEntity> findByBatchId(UUID batchId, Pageable pageable);
    long countByBatchId(UUID batchId);
    long countQuarantinedByBatchId(UUID batchId);
}
