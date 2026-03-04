package com.derbysoft.click.modules.organisationstructure.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChainRequest(
    @NotBlank String name,
    String timezone,
    String currency
) {}
