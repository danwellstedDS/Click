package com.derbysoft.click.modules.ingestion.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentAutoClosed;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentEscalated;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentOpened;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentReopened;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.IncidentStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SyncIncidentTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String KEY = "integration-1:acct:2026-03-01:2026-03-04:CAMPAIGN_PERFORMANCE";
    private static final Instant NOW = Instant.parse("2026-03-05T02:00:00Z");

    private SyncIncident openIncident() {
        return SyncIncident.open(ID, KEY, TENANT_ID, FailureClass.TRANSIENT, NOW);
    }

    @Test
    void shouldOpenIncidentWithOneFailure() {
        SyncIncident incident = openIncident();

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(incident.getConsecutiveFailures()).isEqualTo(1);
        assertThat(incident.getEvents()).hasSize(1);
        assertThat(incident.getEvents().get(0)).isInstanceOf(SyncIncidentOpened.class);
    }

    @Test
    void shouldEscalateAfterThreeConsecutiveFailures() {
        SyncIncident incident = openIncident();
        incident.clearEvents();

        incident.recordFailure(NOW);
        incident.recordFailure(NOW);

        assertThat(incident.getConsecutiveFailures()).isEqualTo(3);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.ESCALATED);
        boolean hasEscalated = incident.getEvents().stream()
            .anyMatch(e -> e instanceof SyncIncidentEscalated);
        assertThat(hasEscalated).isTrue();
    }

    @Test
    void shouldAutoCloseOpenIncident() {
        SyncIncident incident = openIncident();
        incident.clearEvents();

        incident.autoClose(NOW);

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.AUTO_CLOSED);
        assertThat(incident.getEvents()).hasSize(1);
        assertThat(incident.getEvents().get(0)).isInstanceOf(SyncIncidentAutoClosed.class);
    }

    @Test
    void shouldReopenAutoClosedIncidentOnRecurrence() {
        SyncIncident incident = openIncident();
        incident.autoClose(NOW);
        incident.clearEvents();

        incident.recordFailure(NOW);

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.REOPENED);
        assertThat(incident.getEvents()).hasSize(1);
        assertThat(incident.getEvents().get(0)).isInstanceOf(SyncIncidentReopened.class);
    }

    @Test
    void shouldAcknowledgeEscalatedIncident() {
        SyncIncident incident = openIncident();
        incident.recordFailure(NOW);
        incident.recordFailure(NOW);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.ESCALATED);

        incident.acknowledge("confirmed on-call review", "ops-user", NOW);

        assertThat(incident.getAcknowledgedBy()).isEqualTo("ops-user");
        assertThat(incident.getAckReason()).isEqualTo("confirmed on-call review");
        assertThat(incident.getAcknowledgedAt()).isEqualTo(NOW);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.ESCALATED);
    }

    @Test
    void shouldThrowConflictWhenAcknowledgingNonEscalated() {
        SyncIncident incident = openIncident();

        assertThatThrownBy(() -> incident.acknowledge("reason", "user", NOW))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("ESCALATED");
    }
}
