package com.derbysoft.click.modules.normalisation.interfaces.http.dto;

import java.util.Map;

public record NormalizationQualityReport(
    long totalFacts,
    long quarantinedFacts,
    Map<String, Long> flagBreakdown
) {}
