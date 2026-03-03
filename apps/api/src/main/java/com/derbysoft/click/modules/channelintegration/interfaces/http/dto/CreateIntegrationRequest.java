package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateIntegrationRequest(
    @NotNull UUID tenantId,
    @NotNull Channel channel,
    String connectionKey,
    @NotNull CadenceType cadenceType,
    String cronExpression,
    Integer intervalMinutes,
    @NotBlank String timezone
) {}
