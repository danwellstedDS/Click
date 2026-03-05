package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.domain.entities.AccountGraphState;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper.AccountGraphStateMapper;
import java.util.List;
import java.util.UUID;

public class AccountGraphStateRepositoryImpl implements AccountGraphStateRepository {

    private final AccountGraphStateJpaRepository jpaRepository;
    private final AccountGraphStateMapper mapper;

    public AccountGraphStateRepositoryImpl(
        AccountGraphStateJpaRepository jpaRepository,
        AccountGraphStateMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<AccountGraphState> findByConnectionId(UUID connectionId) {
        return jpaRepository.findByConnectionId(connectionId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void deleteByConnectionId(UUID connectionId) {
        jpaRepository.deleteByConnectionId(connectionId);
    }

    @Override
    public AccountGraphState save(AccountGraphState state) {
        var entity = mapper.toEntity(state);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
