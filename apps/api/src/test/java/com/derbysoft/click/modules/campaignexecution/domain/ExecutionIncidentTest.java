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
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String KEY = "revision-1:item-1:CREATE_CAMPAIGN:0";
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    private ExecutionIncident openIncident() {
        return ExecutionIncident.open(ID, KEY, TENANT_ID, FailureClass.TRANSIENT, NOW);
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
