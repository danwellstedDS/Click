package com.derbysoft.click.modules.channelintegration.domain;

import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationInstanceRepository {
    Optional<IntegrationInstance> findById(UUID id);
    Optional<IntegrationInstance> findByTenantIdAndChannelAndConnectionKey(UUID tenantId, Channel channel, String connectionKey);
    List<IntegrationInstance> findAllByTenantId(UUID tenantId);
    List<IntegrationInstance> findAllSchedulable();
    IntegrationInstance save(IntegrationInstance instance);
    void deleteById(UUID id);
}
