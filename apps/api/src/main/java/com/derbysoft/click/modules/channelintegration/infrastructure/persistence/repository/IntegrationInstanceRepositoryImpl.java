package com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository;

import com.derbysoft.click.modules.channelintegration.api.contracts.IntegrationInstanceInfo;
import com.derbysoft.click.modules.channelintegration.api.ports.IntegrationQueryPort;
import com.derbysoft.click.modules.channelintegration.domain.IntegrationInstanceRepository;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.entity.IntegrationInstanceEntity;
import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.mapper.IntegrationInstanceMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IntegrationInstanceRepositoryImpl
    implements IntegrationInstanceRepository, IntegrationQueryPort {

    private final IntegrationInstanceJpaRepository jpaRepository;
    private final IntegrationInstanceMapper mapper;

    public IntegrationInstanceRepositoryImpl(
        IntegrationInstanceJpaRepository jpaRepository,
        IntegrationInstanceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<IntegrationInstance> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<IntegrationInstance> findByTenantIdAndChannelAndConnectionKey(
        UUID tenantId, Channel channel, String connectionKey
    ) {
        return jpaRepository.findByTenantIdAndChannelAndConnectionKey(
            tenantId, channel.name(), connectionKey
        ).map(mapper::toDomain);
    }

    @Override
    public List<IntegrationInstance> findAllByTenantId(UUID tenantId) {
        return jpaRepository.findAllByTenantId(tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<IntegrationInstance> findAllSchedulable() {
        return jpaRepository.findAllByStatusAndCadenceTypeNot("Active", CadenceType.MANUAL.name())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public IntegrationInstance save(IntegrationInstance instance) {
        IntegrationInstanceEntity entity = mapper.toEntity(instance);
        IntegrationInstanceEntity saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    // IntegrationQueryPort

    @Override
    public Optional<IntegrationInstanceInfo> findInfoById(UUID id) {
        return jpaRepository.findById(id)
            .map(e -> new IntegrationInstanceInfo(
                e.getId(),
                e.getTenantId(),
                Channel.valueOf(e.getChannel()),
                e.getStatus()
            ));
    }

    @Override
    public boolean isActive(UUID tenantId, Channel channel) {
        return jpaRepository.findByTenantIdAndChannelAndConnectionKey(tenantId, channel.name(), "default")
            .map(e -> "Active".equals(e.getStatus()))
            .orElse(false);
    }
}
