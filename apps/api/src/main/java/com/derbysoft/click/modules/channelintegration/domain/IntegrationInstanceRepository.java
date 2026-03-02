package com.derbysoft.click.modules.channelintegration.domain;

import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationInstanceRepository {
    Optional<IntegrationInstance> findById(UUID id);
    Optional<IntegrationInstance> findByTenantIdAndChannel(UUID tenantId, Channel channel);
    List<IntegrationInstance> findAllByTenantId(UUID tenantId);
    List<IntegrationInstance> findAllByStatus(IntegrationStatus status);
    IntegrationInstance save(IntegrationInstance instance);
    void deleteById(UUID id);
}
