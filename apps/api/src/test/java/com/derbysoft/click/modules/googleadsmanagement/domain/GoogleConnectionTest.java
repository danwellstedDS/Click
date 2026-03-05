package com.derbysoft.click.modules.googleadsmanagement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionBroken;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionCreated;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionCredentialRotated;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionRecovered;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.ConnectionStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GoogleConnectionTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String MANAGER_ID = "858-270-7576";
    private static final String CREDENTIAL_PATH = "infra/secrets/google-search-creds.json";

    private static GoogleConnection newConnection() {
        return GoogleConnection.create(UUID.randomUUID(), TENANT_ID, MANAGER_ID, CREDENTIAL_PATH, Instant.now());
    }

    @Test
    void shouldCreateConnectionInActiveStatus() {
        GoogleConnection conn = newConnection();
        assertThat(conn.getStatus()).isEqualTo(ConnectionStatus.ACTIVE);
        assertThat(conn.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(conn.getManagerId()).isEqualTo(MANAGER_ID);
    }

    @Test
    void shouldEmitConnectionCreatedOnCreate() {
        GoogleConnection conn = newConnection();
        assertThat(conn.getEvents()).hasSize(1);
        assertThat(conn.getEvents().get(0)).isInstanceOf(ConnectionCreated.class);
    }

    @Test
    void shouldRotateCredentialAndEmitEvent() {
        GoogleConnection conn = newConnection();
        conn.clearEvents();
        conn.rotateCredential("new/path/creds.json");
        assertThat(conn.getCredentialPath()).isEqualTo("new/path/creds.json");
        assertThat(conn.getEvents()).hasSize(1);
        assertThat(conn.getEvents().get(0)).isInstanceOf(ConnectionCredentialRotated.class);
    }

    @Test
    void shouldMarkBrokenAndEmitEvent() {
        GoogleConnection conn = newConnection();
        conn.clearEvents();
        conn.markBroken("API error");
        assertThat(conn.getStatus()).isEqualTo(ConnectionStatus.BROKEN);
        assertThat(conn.getEvents()).hasSize(1);
        assertThat(conn.getEvents().get(0)).isInstanceOf(ConnectionBroken.class);
    }

    @Test
    void shouldRecoverFromBrokenAndEmitEvent() {
        GoogleConnection conn = newConnection();
        conn.markBroken("API error");
        conn.clearEvents();
        conn.recover();
        assertThat(conn.getStatus()).isEqualTo(ConnectionStatus.ACTIVE);
        assertThat(conn.getEvents()).hasSize(1);
        assertThat(conn.getEvents().get(0)).isInstanceOf(ConnectionRecovered.class);
    }

    @Test
    void shouldThrowWhenMarkBrokenOnNonActiveConnection() {
        GoogleConnection conn = newConnection();
        conn.markBroken("first");
        assertThatThrownBy(() -> conn.markBroken("second"))
            .isInstanceOf(DomainError.Conflict.class);
    }
}
