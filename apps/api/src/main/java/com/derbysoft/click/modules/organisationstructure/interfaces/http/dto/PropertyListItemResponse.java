package com.derbysoft.click.modules.organisationstructure.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record PropertyListItemResponse(
    UUID id,
    String name,
    boolean isActive,
    String externalPropertyRef,
    Instant createdAt
) {}
