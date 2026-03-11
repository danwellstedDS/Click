package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappedFactJpaRepository extends JpaRepository<MappedFactEntity, UUID> {

    List<MappedFactEntity> findByMappingRunId(UUID mappingRunId, Pageable pageable);

    long countByMappingRunId(UUID mappingRunId);

    List<MappedFactEntity> findByMappingRunIdAndConfidenceBandIn(
        UUID mappingRunId, List<String> confidenceBands, Pageable pageable);
}
