package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateIntegrationRequest(
    @NotNull UUID tenantId,
    @NotNull Channel channel,
    @NotBlank String cron,
    @NotBlank String timezone
) {}
