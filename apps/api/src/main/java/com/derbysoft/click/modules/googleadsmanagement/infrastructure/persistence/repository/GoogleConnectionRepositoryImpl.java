package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.api.contracts.AccountBindingInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.GoogleAdsConnectionInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper.GoogleConnectionMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements both {@link GoogleConnectionRepository} (BC5 domain port) and
 * {@link GoogleAdsQueryPort} (BC5 public API port). Dual-interface pattern — same as
 * {@code IntegrationInstanceRepositoryImpl} in channelintegration.
 */
public class GoogleConnectionRepositoryImpl
    implements GoogleConnectionRepository, GoogleAdsQueryPort {

    private final GoogleConnectionJpaRepository jpaRepository;
    private final GoogleConnectionMapper mapper;
    private final AccountBindingJpaRepository bindingJpaRepository;

    public GoogleConnectionRepositoryImpl(
        GoogleConnectionJpaRepository jpaRepository,
        GoogleConnectionMapper mapper,
        AccountBindingJpaRepository bindingJpaRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.bindingJpaRepository = bindingJpaRepository;
    }

    @Override
    public Optional<GoogleConnection> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<GoogleConnection> findByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).map(mapper::toDomain);
    }

    @Override
    public List<GoogleConnection> findAllActive() {
        return jpaRepository.findAllByStatus("ACTIVE").stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public GoogleConnection save(GoogleConnection connection) {
        var entity = mapper.toEntity(connection);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    // GoogleAdsQueryPort

    @Override
    public List<GoogleAdsConnectionInfo> findAllActiveConnections() {
        return jpaRepository.findAllByStatus("ACTIVE").stream()
            .map(e -> new GoogleAdsConnectionInfo(e.getId(), e.getTenantId(), e.getManagerId(), e.getStatus()))
            .toList();
    }

    @Override
    public Optional<GoogleAdsConnectionInfo> findConnectionByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId)
            .map(e -> new GoogleAdsConnectionInfo(e.getId(), e.getTenantId(), e.getManagerId(), e.getStatus()));
    }

    @Override
    public List<AccountBindingInfo> listActiveBindings(UUID tenantId) {
        return bindingJpaRepository.findByTenantId(tenantId).stream()
            .filter(e -> BindingStatus.ACTIVE.name().equals(e.getStatus()))
            .map(e -> new AccountBindingInfo(
                e.getId(), e.getTenantId(), e.getCustomerId(), e.getStatus(), e.getBindingType()
            ))
            .toList();
    }
}
