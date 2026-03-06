package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;

public record ForceRunItemRequest(@NotBlank String reason) {}
