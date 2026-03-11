package com.derbysoft.click.modules.normalisation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchFailed;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchProduced;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchStarted;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.BatchStatus;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CanonicalBatchTest {

    private static final UUID SNAPSHOT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "123";
    private static final MappingVersion VERSION = MappingVersion.V1;
    private static final Instant NOW = Instant.now();

    private CanonicalBatch createBatch() {
        return CanonicalBatch.create(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION, NOW);
    }

    @Test
    void shouldCreateInProcessingState() {
        CanonicalBatch batch = createBatch();

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PROCESSING);
        assertThat(batch.getSourceSnapshotId()).isEqualTo(SNAPSHOT_ID);
        assertThat(batch.getIntegrationId()).isEqualTo(INTEGRATION_ID);
        assertThat(batch.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(batch.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(batch.getMappingVersion()).isEqualTo(VERSION);
        assertThat(batch.getEvents()).hasSize(1);
        assertThat(batch.getEvents().get(0)).isInstanceOf(CanonicalBatchStarted.class);
    }

    @Test
    void shouldEmitCanonicalBatchStartedOnCreate() {
        CanonicalBatch batch = createBatch();

        CanonicalBatchStarted event = (CanonicalBatchStarted) batch.getEvents().get(0);
        assertThat(event.batchId()).isEqualTo(batch.getId());
        assertThat(event.snapshotId()).isEqualTo(SNAPSHOT_ID);
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.mappingVersion()).isEqualTo(VERSION.value());
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }

    @Test
    void shouldGenerateDeterministicId() {
        CanonicalBatch batch1 = CanonicalBatch.create(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION, NOW);
        CanonicalBatch batch2 = CanonicalBatch.create(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION, NOW);

        assertThat(batch1.getId()).isEqualTo(batch2.getId());
    }

    @Test
    void shouldProduceWithCounts() {
        CanonicalBatch batch = createBatch();
        batch.clearEvents();

        batch.produce(10, 2, "checksum-abc", NOW);

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PRODUCED);
        assertThat(batch.getFactCount()).isEqualTo(10);
        assertThat(batch.getQuarantinedCount()).isEqualTo(2);
        assertThat(batch.getChecksum()).isEqualTo("checksum-abc");
        assertThat(batch.getEvents()).hasSize(1);
        assertThat(batch.getEvents().get(0)).isInstanceOf(CanonicalBatchProduced.class);
    }

    @Test
    void shouldEmitCanonicalBatchProducedWithCorrectFields() {
        CanonicalBatch batch = createBatch();
        batch.clearEvents();
        batch.produce(10, 2, "checksum-abc", NOW);

        CanonicalBatchProduced event = (CanonicalBatchProduced) batch.getEvents().get(0);
        assertThat(event.batchId()).isEqualTo(batch.getId());
        assertThat(event.factCount()).isEqualTo(10);
        assertThat(event.quarantinedCount()).isEqualTo(2);
        assertThat(event.checksum()).isEqualTo("checksum-abc");
        assertThat(event.channel()).isEqualTo("GOOGLE_ADS");
    }

    @Test
    void shouldBeImmutableOnceProduced() {
        CanonicalBatch batch = createBatch();
        batch.produce(10, 0, "checksum", NOW);

        assertThatThrownBy(() -> batch.produce(5, 0, "checksum2", NOW))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PRODUCED");
    }

    @Test
    void shouldFailWithReason() {
        CanonicalBatch batch = createBatch();
        batch.clearEvents();

        batch.fail("upstream error", NOW);

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(batch.getFailureReason()).isEqualTo("upstream error");
        assertThat(batch.getEvents()).hasSize(1);
        assertThat(batch.getEvents().get(0)).isInstanceOf(CanonicalBatchFailed.class);
    }

    @Test
    void shouldEmitCanonicalBatchFailedWithCorrectReason() {
        CanonicalBatch batch = createBatch();
        batch.clearEvents();
        batch.fail("upstream error", NOW);

        CanonicalBatchFailed event = (CanonicalBatchFailed) batch.getEvents().get(0);
        assertThat(event.failureReason()).isEqualTo("upstream error");
        assertThat(event.batchId()).isEqualTo(batch.getId());
        assertThat(event.snapshotId()).isEqualTo(SNAPSHOT_ID);
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void clearEventsShouldEmptyList() {
        CanonicalBatch batch = createBatch();
        assertThat(batch.getEvents()).isNotEmpty();

        batch.clearEvents();

        assertThat(batch.getEvents()).isEmpty();
    }
}
