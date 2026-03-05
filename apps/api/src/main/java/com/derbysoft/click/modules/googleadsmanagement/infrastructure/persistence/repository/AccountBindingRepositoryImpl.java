package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.domain.AccountBindingRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper.AccountBindingMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountBindingRepositoryImpl implements AccountBindingRepository {

    private final AccountBindingJpaRepository jpaRepository;
    private final AccountBindingMapper mapper;

    public AccountBindingRepositoryImpl(
        AccountBindingJpaRepository jpaRepository,
        AccountBindingMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AccountBinding> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AccountBinding> findByConnectionId(UUID connectionId) {
        return jpaRepository.findByConnectionId(connectionId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<AccountBinding> findByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<AccountBinding> findByConnectionIdAndCustomerId(UUID connectionId, String customerId) {
        return jpaRepository.findByConnectionIdAndCustomerId(connectionId, customerId)
            .map(mapper::toDomain);
    }

    @Override
    public AccountBinding save(AccountBinding binding) {
        var entity = mapper.toEntity(binding);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
