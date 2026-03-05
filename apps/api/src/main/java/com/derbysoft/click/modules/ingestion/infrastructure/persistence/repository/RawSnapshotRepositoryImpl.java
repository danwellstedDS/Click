package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.domain.RawSnapshotRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.RawSnapshot;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.RawSnapshotMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RawSnapshotRepositoryImpl implements RawSnapshotRepository {

    private final RawSnapshotJpaRepository jpaRepository;
    private final RawSnapshotMapper mapper;

    public RawSnapshotRepositoryImpl(RawSnapshotJpaRepository jpaRepository, RawSnapshotMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RawSnapshot> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RawSnapshot> findByJobId(UUID jobId) {
        return jpaRepository.findBySyncJobId(jobId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public RawSnapshot save(RawSnapshot snapshot) {
        var entity = mapper.toEntity(snapshot);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
