package com.derbysoft.click.modules.attributionmapping.application.services;

import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort.ActiveBindingData;
import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort.CanonicalFactData;
import com.derbysoft.click.modules.attributionmapping.application.services.ConfidenceScorer.ScoreResult;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MappingEngine {

    private final OverrideResolver overrideResolver;
    private final ConfidenceScorer confidenceScorer;

    public MappingEngine(OverrideResolver overrideResolver, ConfidenceScorer confidenceScorer) {
        this.overrideResolver = overrideResolver;
        this.confidenceScorer = confidenceScorer;
    }

    public MappedFactEntity resolve(
        CanonicalFactData fact,
        Map<String, ActiveBindingData> bindingsByCustomerId,
        UUID runId,
        String ruleSetVersion,
        Instant now
    ) {
        MappedFactEntity entity = new MappedFactEntity();
        entity.setId(UUID.randomUUID());
        entity.setMappingRunId(runId);
        entity.setCanonicalFactId(fact.id());
        entity.setTenantId(fact.tenantId());
        entity.setRuleSetVersion(ruleSetVersion);
        entity.setMappedAt(now);

        // 1. Check for manual override (highest precedence)
        Optional<MappingOverrideEntity> override = overrideResolver.resolve(
            fact.tenantId(), fact.customerAccountId(), fact.campaignId());

        if (override.isPresent()) {
            MappingOverrideEntity o = override.get();
            ScoreResult score = confidenceScorer.scoreOverride();
            entity.setResolvedOrgNodeId(o.getTargetOrgNodeId());
            entity.setResolvedScopeType(o.getTargetScopeType());
            entity.setConfidenceBand(score.band().name());
            entity.setConfidenceScore(score.score());
            entity.setResolutionReasonCode("MANUAL_OVERRIDE");
            entity.setOverrideApplied(true);
            return entity;
        }

        // 2. Explicit BC5 binding with orgNodeId
        ActiveBindingData binding = bindingsByCustomerId.get(fact.customerAccountId());
        if (binding != null && binding.orgNodeId() != null) {
            ScoreResult score = confidenceScorer.scoreExplicitBinding();
            entity.setResolvedOrgNodeId(binding.orgNodeId());
            entity.setResolvedScopeType(binding.orgScopeType());
            entity.setConfidenceBand(score.band().name());
            entity.setConfidenceScore(score.score());
            entity.setResolutionReasonCode("EXPLICIT_BINDING");
            entity.setOverrideApplied(false);
            return entity;
        }

        // 3. Unresolved
        ScoreResult score = confidenceScorer.scoreUnresolved();
        entity.setResolvedOrgNodeId(null);
        entity.setResolvedScopeType(null);
        entity.setConfidenceBand(score.band().name());
        entity.setConfidenceScore(score.score());
        entity.setResolutionReasonCode("NO_MATCH");
        entity.setOverrideApplied(false);
        return entity;
    }

    public static Map<String, ActiveBindingData> indexByCustomerId(List<ActiveBindingData> bindings) {
        return bindings.stream()
            .collect(Collectors.toMap(ActiveBindingData::customerId, b -> b, (a, b) -> a));
    }
}
