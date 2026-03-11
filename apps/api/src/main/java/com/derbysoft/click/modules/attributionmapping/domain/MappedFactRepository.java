package com.derbysoft.click.modules.attributionmapping.domain;

import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MappedFactRepository {
    void saveAll(List<MappedFactEntity> facts);
    List<MappedFactEntity> findByMappingRunId(UUID runId, Pageable pageable);
    long countByMappingRunId(UUID runId);
    List<MappedFactEntity> findLowConfidenceByMappingRunId(UUID runId, Pageable pageable);
}
