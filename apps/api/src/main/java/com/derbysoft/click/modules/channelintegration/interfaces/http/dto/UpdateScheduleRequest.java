package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateScheduleRequest(
    @NotNull CadenceType cadenceType,
    String cronExpression,
    Integer intervalMinutes,
    @NotBlank String timezone
) {}
