package com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository;

import com.derbysoft.click.modules.normalisation.domain.CanonicalFactRepository;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class CanonicalFactRepositoryImpl implements CanonicalFactRepository {

    private final CanonicalFactJpaRepository jpaRepository;

    public CanonicalFactRepositoryImpl(CanonicalFactJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<CanonicalFactEntity> facts) {
        jpaRepository.saveAllAndFlush(facts);
    }

    @Override
    public List<CanonicalFactEntity> findByBatchId(UUID batchId, Pageable pageable) {
        return jpaRepository.findByCanonicalBatchId(batchId, pageable);
    }

    @Override
    public long countByBatchId(UUID batchId) {
        return jpaRepository.countByCanonicalBatchId(batchId);
    }

    @Override
    public long countQuarantinedByBatchId(UUID batchId) {
        return jpaRepository.countByCanonicalBatchIdAndQuarantinedTrue(batchId);
    }
}
