package com.derbysoft.click.modules.normalisation.api.contracts;

import java.util.Map;

public record NormalisationQualityStats(
    long totalFacts,
    long quarantinedFacts,
    Map<String, Long> flagBreakdown
) {}
