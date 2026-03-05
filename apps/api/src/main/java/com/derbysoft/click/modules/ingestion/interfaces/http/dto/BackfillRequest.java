package com.derbysoft.click.modules.ingestion.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BackfillRequest(
    @NotNull UUID integrationId,
    @NotBlank String accountId,
    @NotNull String dateFrom,
    @NotNull String dateTo,
    @NotBlank String reason
) {}
