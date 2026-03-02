package com.derbysoft.click.modules.channelintegration.api.contracts;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import java.util.UUID;

public record IntegrationInstanceInfo(
    UUID id,
    UUID tenantId,
    Channel channel,
    String status
) {}
