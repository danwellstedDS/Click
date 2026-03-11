package com.derbysoft.click.modules.attributionmapping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import com.derbysoft.click.modules.attributionmapping.domain.events.LowConfidenceMappingDetected;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingResultBatchProduced;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingRunFailed;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingRunStarted;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.RunStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MappingRunTest {

    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();

    @Test
    void shouldCreateInRunningState() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());

        assertThat(run.getStatus()).isEqualTo(RunStatus.RUNNING);
        assertThat(run.getCanonicalBatchId()).isEqualTo(BATCH_ID);
        assertThat(run.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(run.getRuleSetVersion()).isEqualTo("v1");
        assertThat(run.getOverrideSetVersion()).isEqualTo("abc123");
        assertThat(run.getEvents()).hasSize(1);
        assertThat(run.getEvents().get(0)).isInstanceOf(MappingRunStarted.class);
    }

    @Test
    void shouldGenerateDeterministicId() {
        MappingRun run1 = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        MappingRun run2 = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());

        assertThat(run1.getId()).isEqualTo(run2.getId());
    }

    @Test
    void shouldProduceWithCounts() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        run.clearEvents();

        run.produce(90, 0, 0, List.of(), Instant.now());

        assertThat(run.getStatus()).isEqualTo(RunStatus.PRODUCED);
        assertThat(run.getMappedCount()).isEqualTo(90);
        assertThat(run.getLowConfidenceCount()).isZero();
        assertThat(run.getUnresolvedCount()).isZero();
        assertThat(run.getEvents()).hasSize(1);
        assertThat(run.getEvents().get(0)).isInstanceOf(MappingResultBatchProduced.class);
    }

    @Test
    void shouldFailWithReason() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        run.clearEvents();

        run.fail("upstream error", Instant.now());

        assertThat(run.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.getFailureReason()).isEqualTo("upstream error");
        assertThat(run.getEvents()).hasSize(1);
        assertThat(run.getEvents().get(0)).isInstanceOf(MappingRunFailed.class);
    }

    @Test
    void shouldBeImmutableOnceProduced() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        run.produce(10, 0, 0, List.of(), Instant.now());

        assertThatThrownBy(() -> run.produce(20, 0, 0, List.of(), Instant.now()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PRODUCED");
    }

    @Test
    void clearEventsShouldEmptyList() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        assertThat(run.getEvents()).isNotEmpty();

        run.clearEvents();

        assertThat(run.getEvents()).isEmpty();
    }

    @Test
    void shouldEmitLowConfidenceMappingDetectedWhenLowOrUnresolved() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        run.clearEvents();
        UUID factId = UUID.randomUUID();

        run.produce(3, 1, 1, List.of(factId), Instant.now());

        assertThat(run.getEvents()).hasSize(2);
        assertThat(run.getEvents().get(0)).isInstanceOf(MappingResultBatchProduced.class);
        LowConfidenceMappingDetected evt = (LowConfidenceMappingDetected) run.getEvents().get(1);
        assertThat(evt.lowConfidenceCount()).isEqualTo(1);
        assertThat(evt.unresolvedCount()).isEqualTo(1);
        assertThat(evt.lowConfidenceFactIds()).containsExactly(factId);
    }

    @Test
    void shouldNotEmitLowConfidenceMappingDetectedWhenAllHigh() {
        MappingRun run = MappingRun.create(BATCH_ID, TENANT_ID, "v1", "abc123", Instant.now());
        run.clearEvents();

        run.produce(3, 0, 0, List.of(), Instant.now());

        assertThat(run.getEvents()).hasSize(1);
        assertThat(run.getEvents().get(0)).isInstanceOf(MappingResultBatchProduced.class);
    }
}
