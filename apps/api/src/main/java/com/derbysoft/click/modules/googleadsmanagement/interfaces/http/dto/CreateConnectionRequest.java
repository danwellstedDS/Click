package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateConnectionRequest(
    @NotNull UUID tenantId,
    @NotBlank String managerId,
    String credentialPath
) {}
