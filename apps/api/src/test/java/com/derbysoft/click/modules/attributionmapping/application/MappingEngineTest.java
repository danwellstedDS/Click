package com.derbysoft.click.modules.attributionmapping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort.ActiveBindingData;
import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort.CanonicalFactData;
import com.derbysoft.click.modules.attributionmapping.application.services.ConfidenceScorer;
import com.derbysoft.click.modules.attributionmapping.application.services.MappingEngine;
import com.derbysoft.click.modules.attributionmapping.application.services.OverrideResolver;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappedFactEntity;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MappingEngineTest {

    @Mock OverrideResolver overrideResolver;

    private MappingEngine engine;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ORG_NODE_ID = UUID.randomUUID();
    private static final UUID RUN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        engine = new MappingEngine(overrideResolver, new ConfidenceScorer());
    }

    private CanonicalFactData fact(String accountId, String campaignId) {
        return new CanonicalFactData(
            UUID.randomUUID(), TENANT_ID, "GOOGLE_ADS", UUID.randomUUID(),
            accountId, campaignId, "Test Campaign", LocalDate.now(),
            1000L, 50L, 500000L, BigDecimal.valueOf(5.0), false
        );
    }

    @Test
    void shouldApplyOverrideWithHighConfidence() {
        MappingOverrideEntity override = new MappingOverrideEntity();
        override.setTargetOrgNodeId(ORG_NODE_ID);
        override.setTargetScopeType("Property");

        when(overrideResolver.resolve(any(), anyString(), anyString()))
            .thenReturn(Optional.of(override));

        CanonicalFactData factData = fact("123", "campaign-1");
        Map<String, ActiveBindingData> bindings = Map.of();

        MappedFactEntity result = engine.resolve(factData, bindings, RUN_ID, "v1", Instant.now());

        assertThat(result.getConfidenceBand()).isEqualTo("HIGH");
        assertThat(result.getConfidenceScore()).isEqualByComparingTo("1.000");
        assertThat(result.getResolutionReasonCode()).isEqualTo("MANUAL_OVERRIDE");
        assertThat(result.isOverrideApplied()).isTrue();
        assertThat(result.getResolvedOrgNodeId()).isEqualTo(ORG_NODE_ID);
    }

    @Test
    void shouldUseExplicitBindingWhenNoOverride() {
        when(overrideResolver.resolve(any(), anyString(), anyString()))
            .thenReturn(Optional.empty());

        UUID bindingOrgNode = UUID.randomUUID();
        ActiveBindingData binding = new ActiveBindingData(UUID.randomUUID(), "123", bindingOrgNode, "Property");
        Map<String, ActiveBindingData> bindings = MappingEngine.indexByCustomerId(List.of(binding));

        MappedFactEntity result = engine.resolve(fact("123", "campaign-1"), bindings, RUN_ID, "v1", Instant.now());

        assertThat(result.getConfidenceBand()).isEqualTo("HIGH");
        assertThat(result.getConfidenceScore()).isEqualByComparingTo("0.900");
        assertThat(result.getResolutionReasonCode()).isEqualTo("EXPLICIT_BINDING");
        assertThat(result.isOverrideApplied()).isFalse();
        assertThat(result.getResolvedOrgNodeId()).isEqualTo(bindingOrgNode);
    }

    @Test
    void shouldBeUnresolvedWhenNoBindingOrgNode() {
        when(overrideResolver.resolve(any(), anyString(), anyString()))
            .thenReturn(Optional.empty());

        ActiveBindingData bindingNoOrg = new ActiveBindingData(UUID.randomUUID(), "123", null, null);
        Map<String, ActiveBindingData> bindings = MappingEngine.indexByCustomerId(List.of(bindingNoOrg));

        MappedFactEntity result = engine.resolve(fact("123", "campaign-1"), bindings, RUN_ID, "v1", Instant.now());

        assertThat(result.getConfidenceBand()).isEqualTo("UNRESOLVED");
        assertThat(result.getResolutionReasonCode()).isEqualTo("NO_MATCH");
        assertThat(result.getResolvedOrgNodeId()).isNull();
    }

    @Test
    void shouldBeUnresolvedWhenNoBindingAtAll() {
        when(overrideResolver.resolve(any(), any(), any()))
            .thenReturn(Optional.empty());

        MappedFactEntity result = engine.resolve(fact("unknown-account", null), Map.of(), RUN_ID, "v1", Instant.now());

        assertThat(result.getConfidenceBand()).isEqualTo("UNRESOLVED");
        assertThat(result.getResolutionReasonCode()).isEqualTo("NO_MATCH");
    }
}
