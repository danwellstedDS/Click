package com.derbysoft.click.modules.attributionmapping.application.services;

import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.ConfidenceBand;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ConfidenceScorer {

    public static final BigDecimal SCORE_OVERRIDE = new BigDecimal("1.000");
    public static final BigDecimal SCORE_EXPLICIT_BINDING = new BigDecimal("0.900");
    public static final BigDecimal SCORE_UNRESOLVED = BigDecimal.ZERO;

    public record ScoreResult(ConfidenceBand band, BigDecimal score) {}

    public ScoreResult scoreOverride() {
        return new ScoreResult(ConfidenceBand.HIGH, SCORE_OVERRIDE);
    }

    public ScoreResult scoreExplicitBinding() {
        return new ScoreResult(ConfidenceBand.HIGH, SCORE_EXPLICIT_BINDING);
    }

    public ScoreResult scoreUnresolved() {
        return new ScoreResult(ConfidenceBand.UNRESOLVED, SCORE_UNRESOLVED);
    }
}
