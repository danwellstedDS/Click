package com.derbysoft.click.modules.channelintegration.api.ports;

import com.derbysoft.click.modules.channelintegration.api.contracts.IntegrationInstanceInfo;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationQueryPort {
    Optional<IntegrationInstanceInfo> findInfoById(UUID id);
    boolean isActive(UUID tenantId, Channel channel);
}
