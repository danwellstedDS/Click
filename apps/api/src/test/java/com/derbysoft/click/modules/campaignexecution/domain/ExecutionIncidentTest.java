package com.derbysoft.click.modules.campaignexecution.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentAutoClosed;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentEscalated;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentOpened;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentReopened;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.IncidentStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExecutionIncidentTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID REVISION_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String FC_KEY = "TRANSIENT";
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private ExecutionIncident openIncident() {
        return ExecutionIncident.open(ID, REVISION_ID, ITEM_ID, FC_KEY, TENANT_ID,
            FailureClass.TRANSIENT, NOW);
    }

    @Test
    void shouldOpenIncidentWithOneFailure() {
        ExecutionIncident incident = openIncident();

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(incident.getConsecutiveFailures()).isEqualTo(1);
        assertThat(incident.getEvents()).hasSize(1);
        assertThat(incident.getEvents().get(0)).isInstanceOf(ExecutionIncidentOpened.class);
    }

    @Test
    void shouldExposeCompositeIdentityFields() {
        ExecutionIncident incident = openIncident();

        assertThat(incident.getRevisionId()).isEqualTo(REVISION_ID);
        assertThat(incident.getItemId()).isEqualTo(ITEM_ID);
        assertThat(incident.getFailureClassKey()).isEqualTo(FC_KEY);
    }

    @Test
    void shouldEscalateAfterThreeConsecutiveFailures() {
        ExecutionIncident incident = openIncident();
        incident.clearEvents();

        incident.recordFailure(NOW);
        incident.recordFailure(NOW);

        assertThat(incident.getConsecutiveFailures()).isEqualTo(3);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.ESCALATED);
        boolean hasEscalated = incident.getEvents().stream()
            .anyMatch(e -> e instanceof ExecutionIncidentEscalated);
        assertThat(hasEscalated).isTrue();
    }

    @Test
    void shouldAutoCloseIncident() {
        ExecutionIncident incident = openIncident();
        incident.clearEvents();

        incident.autoClose(NOW);

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.AUTO_CLOSED);
        assertThat(incident.getEvents()).hasSize(1);
        assertThat(incident.getEvents().get(0)).isInstanceOf(ExecutionIncidentAutoClosed.class);
    }

    @Test
    void shouldReopenAutoClosedIncidentOnRecurrence() {
        ExecutionIncident incident = openIncident();
        incident.autoClose(NOW);
        incident.clearEvents();

        incident.recordFailure(NOW);

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.REOPENED);
        assertThat(incident.getEvents()).hasSize(1);
        assertThat(incident.getEvents().get(0)).isInstanceOf(ExecutionIncidentReopened.class);
    }

    @Test
    void shouldNotExpireRecurrenceWindowWithin24h() {
        ExecutionIncident incident = openIncident();
        incident.autoClose(NOW);

        Instant within24h = NOW.plusSeconds(3600 * 23);
        assertThat(incident.isRecurrenceWindowExpired(within24h)).isFalse();
    }

    @Test
    void shouldExpireRecurrenceWindowAfter24h() {
        ExecutionIncident incident = openIncident();
        incident.autoClose(NOW);

        Instant beyond24h = NOW.plusSeconds(3600 * 25);
        assertThat(incident.isRecurrenceWindowExpired(beyond24h)).isTrue();
    }

    @Test
    void shouldAcknowledgeEscalatedIncident() {
        ExecutionIncident incident = openIncident();
        incident.recordFailure(NOW);
        incident.recordFailure(NOW);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.ESCALATED);

        incident.acknowledge("root cause confirmed", "ops-user", NOW);

        assertThat(incident.getAcknowledgedBy()).isEqualTo("ops-user");
        assertThat(incident.getAckReason()).isEqualTo("root cause confirmed");
        assertThat(incident.getAcknowledgedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldThrowConflictWhenAcknowledgingNonEscalated() {
        ExecutionIncident incident = openIncident();

        assertThatThrownBy(() -> incident.acknowledge("reason", "user", NOW))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("ESCALATED");
    }
}
