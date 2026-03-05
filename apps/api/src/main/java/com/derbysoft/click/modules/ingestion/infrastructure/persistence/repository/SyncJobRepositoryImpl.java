package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.domain.SyncJobRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper.SyncJobMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SyncJobRepositoryImpl implements SyncJobRepository {

    private final SyncJobJpaRepository jpaRepository;
    private final SyncJobMapper mapper;

    public SyncJobRepositoryImpl(SyncJobJpaRepository jpaRepository, SyncJobMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<SyncJob> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SyncJob> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key).map(mapper::toDomain);
    }

    @Override
    public List<SyncJob> findPendingJobs(Instant maxNextAttemptAfter) {
        return jpaRepository.findPendingJobs(maxNextAttemptAfter).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<SyncJob> findRunningJobsWithExpiredLease(Instant now) {
        return jpaRepository.findRunningJobsWithExpiredLease(now).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<SyncJob> findByIntegrationId(UUID integrationId) {
        return jpaRepository.findByIntegrationId(integrationId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public SyncJob save(SyncJob job) {
        var entity = mapper.toEntity(job);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
