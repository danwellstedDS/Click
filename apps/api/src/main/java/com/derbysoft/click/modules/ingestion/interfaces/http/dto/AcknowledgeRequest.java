package com.derbysoft.click.modules.ingestion.interfaces.http.dto;

import jakarta.validation.constraints.NotBlank;

public record AcknowledgeRequest(
    @NotBlank String ackReason
) {}
