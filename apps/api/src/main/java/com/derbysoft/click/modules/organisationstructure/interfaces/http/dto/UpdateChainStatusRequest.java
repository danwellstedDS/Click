package com.derbysoft.click.modules.organisationstructure.interfaces.http.dto;

import com.derbysoft.click.modules.organisationstructure.domain.valueobjects.ChainStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateChainStatusRequest(
    @NotNull ChainStatus status
) {}
