package com.derbysoft.click.modules.attributionmapping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.attributionmapping.application.handlers.AttributionService;
import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort;
import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort.ActiveBindingData;
import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort;
import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort.CanonicalFactData;
import com.derbysoft.click.modules.attributionmapping.application.services.ConfidenceScorer;
import com.derbysoft.click.modules.attributionmapping.application.services.IdempotencyGuard;
import com.derbysoft.click.modules.attributionmapping.application.services.MappingEngine;
import com.derbysoft.click.modules.attributionmapping.application.services.OverrideResolver;
import com.derbysoft.click.modules.attributionmapping.application.services.OverrideSetVersionComputer;
import com.derbysoft.click.modules.attributionmapping.domain.MappedFactRepository;
import com.derbysoft.click.modules.attributionmapping.domain.MappingRunRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.RunStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttributionServiceTest {

    @Mock MappingRunRepository runRepository;
    @Mock MappedFactRepository factRepository;
    @Mock CanonicalFactQueryPort canonicalFactQueryPort;
    @Mock AccountBindingQueryPort accountBindingQueryPort;
    @Mock IdempotencyGuard idempotencyGuard;
    @Mock OverrideSetVersionComputer overrideSetVersionComputer;
    @Mock InProcessEventBus eventBus;
    @Mock OverrideResolver overrideResolver;

    private AttributionService service;

    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ORG_NODE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MappingEngine mappingEngine = new MappingEngine(overrideResolver, new ConfidenceScorer());
        service = new AttributionService(
            runRepository, factRepository, canonicalFactQueryPort,
            accountBindingQueryPort, mappingEngine, idempotencyGuard,
            overrideSetVersionComputer, eventBus
        );
    }

    private CanonicalFactData fact(String accountId) {
        return new CanonicalFactData(
            UUID.randomUUID(), TENANT_ID, "GOOGLE_ADS", UUID.randomUUID(),
            accountId, "campaign-1", "Test", LocalDate.now(),
            1000L, 50L, 500000L, BigDecimal.valueOf(0.5), BigDecimal.valueOf(5.0), false
        );
    }

    private MappingRun savedRun(MappingRun run) {
        return run;
    }

    @Test
    void shouldMapAllFactsAsHighWhenBindingHasOrgNode() {
        when(overrideSetVersionComputer.compute(TENANT_ID)).thenReturn("overset-1");
        when(idempotencyGuard.check(BATCH_ID, "v1", "overset-1")).thenReturn(Optional.empty());

        List<CanonicalFactData> facts = new ArrayList<>();
        for (int i = 0; i < 3; i++) facts.add(fact("123"));
        when(canonicalFactQueryPort.findByBatchId(BATCH_ID)).thenReturn(facts);
        when(accountBindingQueryPort.findActiveByTenantId(TENANT_ID))
            .thenReturn(List.of(new ActiveBindingData(UUID.randomUUID(), "123", ORG_NODE_ID, "Property")));
        when(overrideResolver.resolve(any(), anyString(), anyString())).thenReturn(Optional.empty());
        when(runRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(runRepository.findById(any())).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            return Optional.of(MappingRun.reconstitute(
                id, BATCH_ID, TENANT_ID, "v1", "overset-1", RunStatus.RUNNING,
                0, 0, 0, Instant.now(), null, null, null, Instant.now(), Instant.now()
            ));
        });

        MappingRun result = service.mapBatch(BATCH_ID, TENANT_ID, "v1");

        assertThat(result.getStatus()).isEqualTo(RunStatus.PRODUCED);
        assertThat(result.getMappedCount()).isEqualTo(3);
        assertThat(result.getUnresolvedCount()).isZero();
        verify(factRepository).saveAll(any());
    }

    @Test
    void shouldReturnExistingRunOnIdempotentReplay() {
        MappingRun existing = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "overset-1", Instant.now());
        existing.produce(5, 0, 0, List.of(), Instant.now());

        when(overrideSetVersionComputer.compute(TENANT_ID)).thenReturn("overset-1");
        when(idempotencyGuard.check(BATCH_ID, "v1", "overset-1")).thenReturn(Optional.of(existing));

        MappingRun result = service.mapBatch(BATCH_ID, TENANT_ID, "v1");

        assertThat(result).isSameAs(existing);
        verify(runRepository, never()).save(any());
        verify(factRepository, never()).saveAll(any());
    }

    @Test
    void shouldMarkUnresolvedWhenNoBindingMatch() {
        when(overrideSetVersionComputer.compute(TENANT_ID)).thenReturn("empty");
        when(idempotencyGuard.check(BATCH_ID, "v1", "empty")).thenReturn(Optional.empty());
        when(canonicalFactQueryPort.findByBatchId(BATCH_ID)).thenReturn(List.of(fact("unknown-account")));
        when(accountBindingQueryPort.findActiveByTenantId(TENANT_ID)).thenReturn(List.of());
        when(overrideResolver.resolve(any(), anyString(), anyString())).thenReturn(Optional.empty());
        when(runRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(runRepository.findById(any())).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            return Optional.of(MappingRun.reconstitute(
                id, BATCH_ID, TENANT_ID, "v1", "empty", RunStatus.RUNNING,
                0, 0, 0, Instant.now(), null, null, null, Instant.now(), Instant.now()
            ));
        });

        MappingRun result = service.mapBatch(BATCH_ID, TENANT_ID, "v1");

        assertThat(result.getUnresolvedCount()).isEqualTo(1);
        assertThat(result.getMappedCount()).isEqualTo(1);
    }
}
