package com.derbysoft.click.modules.normalisation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.derbysoft.click.modules.normalisation.application.services.IdempotencyGuard;
import com.derbysoft.click.modules.normalisation.domain.CanonicalBatchRepository;
import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.BatchStatus;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotencyGuardTest {

    @Mock CanonicalBatchRepository batchRepository;

    private IdempotencyGuard guard;

    private static final UUID SNAPSHOT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "123";
    private static final MappingVersion VERSION = MappingVersion.V1;

    @BeforeEach
    void setUp() {
        guard = new IdempotencyGuard(batchRepository);
    }

    private CanonicalBatch batchWithStatus(BatchStatus status) {
        Instant now = Instant.now();
        CanonicalBatch batch = CanonicalBatch.create(SNAPSHOT_ID, INTEGRATION_ID, TENANT_ID, ACCOUNT_ID, VERSION, now);
        if (status == BatchStatus.PRODUCED) {
            batch.produce(5, 0, "checksum", now);
        } else if (status == BatchStatus.FAILED) {
            batch.fail("some error", now);
        }
        return batch;
    }

    @Test
    void shouldReturnEmptyWhenNoBatchExists() {
        when(batchRepository.findBySourceSnapshotIdAndMappingVersion(SNAPSHOT_ID, VERSION))
            .thenReturn(Optional.empty());

        Optional<CanonicalBatch> result = guard.check(SNAPSHOT_ID, VERSION);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnExistingBatchWhenAlreadyProduced() {
        CanonicalBatch produced = batchWithStatus(BatchStatus.PRODUCED);
        when(batchRepository.findBySourceSnapshotIdAndMappingVersion(SNAPSHOT_ID, VERSION))
            .thenReturn(Optional.of(produced));

        Optional<CanonicalBatch> result = guard.check(SNAPSHOT_ID, VERSION);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(produced);
    }

    @Test
    void shouldReturnExistingBatchWhenFailed() {
        CanonicalBatch failed = batchWithStatus(BatchStatus.FAILED);
        when(batchRepository.findBySourceSnapshotIdAndMappingVersion(SNAPSHOT_ID, VERSION))
            .thenReturn(Optional.of(failed));

        Optional<CanonicalBatch> result = guard.check(SNAPSHOT_ID, VERSION);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(failed);
    }

    @Test
    void shouldThrowWhenBatchIsProcessing() {
        CanonicalBatch processing = batchWithStatus(BatchStatus.PROCESSING);
        when(batchRepository.findBySourceSnapshotIdAndMappingVersion(SNAPSHOT_ID, VERSION))
            .thenReturn(Optional.of(processing));

        assertThatThrownBy(() -> guard.check(SNAPSHOT_ID, VERSION))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PROCESSING");
    }
}
