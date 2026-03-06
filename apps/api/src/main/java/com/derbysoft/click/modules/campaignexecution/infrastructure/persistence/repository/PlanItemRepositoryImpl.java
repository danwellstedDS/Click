package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.PlanItemMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlanItemRepositoryImpl implements PlanItemRepository {

    private final PlanItemJpaRepository jpaRepository;
    private final PlanItemMapper mapper;

    public PlanItemRepositoryImpl(PlanItemJpaRepository jpaRepository, PlanItemMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PlanItem> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PlanItem> findByRevisionId(UUID revisionId) {
        return jpaRepository.findByRevisionId(revisionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PlanItem> findQueuedItems(Instant now) {
        return jpaRepository.findQueuedItems(now).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PlanItem> findByRevisionIdAndStatusIn(UUID revisionId,
                                                       List<PlanItemStatus> statuses) {
        List<String> statusStrings = statuses.stream().map(PlanItemStatus::name).toList();
        return jpaRepository.findByRevisionIdAndStatusIn(revisionId, statusStrings)
            .stream().map(mapper::toDomain).toList();
    }

    @Override
    public PlanItem save(PlanItem item) {
        var entity = mapper.toEntity(item);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<PlanItem> saveAll(List<PlanItem> items) {
        var entities = items.stream().map(mapper::toEntity).toList();
        return jpaRepository.saveAllAndFlush(entities).stream().map(mapper::toDomain).toList();
    }
}
