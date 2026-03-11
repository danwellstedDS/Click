package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.domain.MappedFactRepository;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class MappedFactRepositoryImpl implements MappedFactRepository {

    private final MappedFactJpaRepository jpaRepository;

    public MappedFactRepositoryImpl(MappedFactJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<MappedFactEntity> facts) {
        jpaRepository.saveAll(facts);
        jpaRepository.flush();
    }

    @Override
    public List<MappedFactEntity> findByMappingRunId(UUID runId, Pageable pageable) {
        return jpaRepository.findByMappingRunId(runId, pageable);
    }

    @Override
    public long countByMappingRunId(UUID runId) {
        return jpaRepository.countByMappingRunId(runId);
    }

    @Override
    public List<MappedFactEntity> findLowConfidenceByMappingRunId(UUID runId, Pageable pageable) {
        return jpaRepository.findByMappingRunIdAndConfidenceBandIn(
            runId, List.of("LOW", "UNRESOLVED"), pageable);
    }
}
