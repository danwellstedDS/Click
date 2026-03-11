package com.derbysoft.click.modules.normalisation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.normalisation.application.handlers.NormalisationService;
import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort;
import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort.RawCampaignRowData;
import com.derbysoft.click.modules.normalisation.application.services.BatchAssembler;
import com.derbysoft.click.modules.normalisation.application.services.IdempotencyGuard;
import com.derbysoft.click.modules.normalisation.application.services.Normalizer;
import com.derbysoft.click.modules.normalisation.application.services.QualityValidator;
import com.derbysoft.click.modules.normalisation.domain.CanonicalBatchRepository;
import com.derbysoft.click.modules.normalisation.domain.CanonicalFactRepository;
import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchFailed;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchProduced;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchStarted;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalFactQuarantined;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.BatchStatus;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NormalisationServiceTest {

    @Mock CanonicalBatchRepository batchRepository;
    @Mock CanonicalFactRepository factRepository;
    @Mock RawCampaignRowQueryPort rawCampaignRowQueryPort;
    @Mock IdempotencyGuard idempotencyGuard;
    @Mock InProcessEventBus eventBus;

    QualityValidator qualityValidator = new QualityValidator();
    Normalizer normalizer = new Normalizer();
    BatchAssembler batchAssembler = new BatchAssembler();

    private NormalisationService service;

    private static final UUID SNAPSHOT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "123";
    private static final MappingVersion VERSION = MappingVersion.V1;

    @BeforeEach
    void setUp() {
        service = new NormalisationService(
            batchRepository, factRepository,
            rawCampaignRowQueryPort, normalizer,
            qualityValidator, batchAssembler,
            idempotencyGuard, eventBus
        );

        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idempotencyGuard.check(any(), any())).thenReturn(Optional.empty());
        when(batchRepository.findById(any())).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            return Optional.of(CanonicalBatch.reconstitute(
                id, SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION,
                BatchStatus.PROCESSING, 0, 0, null, null, null, null,
                Instant.now(), Instant.now()
            ));
        });
    }

    private RawCampaignRowData cleanRow(String campaignId) {
        return new RawCampaignRowData(
            UUID.randomUUID(), SNAPSHOT_ID, INTEGRATION_ID,
            ACCOUNT_ID, campaignId, "Test Campaign",
            LocalDate.of(2026, 3, 1), 10L, 100L, 500000L, new BigDecimal("2.0")
        );
    }

    @Test
    void shouldProduceBatchWithAllCleanRows() {
        when(rawCampaignRowQueryPort.findBySnapshotId(SNAPSHOT_ID))
            .thenReturn(List.of(cleanRow("c1"), cleanRow("c2"), cleanRow("c3")));

        CanonicalBatch result = service.normalizeSnapshot(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION);

        assertThat(result.getStatus()).isEqualTo(BatchStatus.PRODUCED);
        assertThat(result.getFactCount()).isEqualTo(3);
        assertThat(result.getQuarantinedCount()).isEqualTo(0);
        verify(factRepository).saveAll(any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(eventBus, atLeastOnce()).publish(captor.capture());
        List<Object> payloads = captor.getAllValues().stream().map(EventEnvelope::payload).toList();
        assertThat(payloads).anyMatch(p -> p instanceof CanonicalBatchStarted);
        assertThat(payloads).anyMatch(p -> p instanceof CanonicalBatchProduced);
    }

    @Test
    void shouldQuarantineRowsWithQualityFlags() {
        RawCampaignRowData badRow = new RawCampaignRowData(
            UUID.randomUUID(), SNAPSHOT_ID, INTEGRATION_ID,
            ACCOUNT_ID, "c2", "Bad Campaign",
            LocalDate.of(2026, 3, 1), 10L, -1L, 500000L, new BigDecimal("2.0")
        );
        when(rawCampaignRowQueryPort.findBySnapshotId(SNAPSHOT_ID))
            .thenReturn(List.of(cleanRow("c1"), badRow));

        CanonicalBatch result = service.normalizeSnapshot(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION);

        assertThat(result.getQuarantinedCount()).isEqualTo(1);
        assertThat(result.getFactCount()).isEqualTo(2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(eventBus, atLeastOnce()).publish(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(e -> e.payload() instanceof CanonicalFactQuarantined);
    }

    @Test
    void shouldReturnExistingBatchOnIdempotentReplay() {
        CanonicalBatch producedBatch = CanonicalBatch.create(
            SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION, Instant.now());
        producedBatch.produce(5, 0, "checksum", Instant.now());

        when(idempotencyGuard.check(SNAPSHOT_ID, VERSION)).thenReturn(Optional.of(producedBatch));

        CanonicalBatch result = service.normalizeSnapshot(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION);

        assertThat(result).isSameAs(producedBatch);
        verify(rawCampaignRowQueryPort, never()).findBySnapshotId(any());
        verify(factRepository, never()).saveAll(any());
    }

    @Test
    void shouldTransitionToFailedOnFetchError() {
        when(rawCampaignRowQueryPort.findBySnapshotId(SNAPSHOT_ID))
            .thenThrow(new RuntimeException("upstream down"));

        assertThatThrownBy(() ->
            service.normalizeSnapshot(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION))
            .isInstanceOf(RuntimeException.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<EventEnvelope<?>> captor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(eventBus, atLeastOnce()).publish(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(e -> e.payload() instanceof CanonicalBatchFailed);
    }
}
