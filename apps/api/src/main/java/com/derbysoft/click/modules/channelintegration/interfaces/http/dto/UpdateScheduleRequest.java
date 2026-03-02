package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateScheduleRequest(
    @NotBlank String cron,
    @NotBlank String timezone
) {}
