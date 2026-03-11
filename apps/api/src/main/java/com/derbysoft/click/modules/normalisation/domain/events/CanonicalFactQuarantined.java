package com.derbysoft.click.modules.normalisation.domain.events;

import com.derbysoft.click.modules.normalisation.domain.valueobjects.QualityFlag;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CanonicalFactQuarantined(
    UUID factId,
    UUID batchId,
    String campaignId,
    LocalDate reportDate,
    List<QualityFlag> qualityFlags,
    Instant occurredAt
) {}
