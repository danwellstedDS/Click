package com.derbysoft.click.modules.googleadsmanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.application.handlers.GoogleConnectionService;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleConnectionServiceTest {

    @Mock
    private GoogleConnectionRepository repository;

    @Mock
    private InProcessEventBus eventBus;

    @InjectMocks
    private GoogleConnectionService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String MANAGER_ID = "858-270-7576";
    private static final String CRED_PATH = "infra/secrets/google-search-creds.json";

    @Test
    void shouldCreateConnectionAndPublishEvent() {
        GoogleConnection conn = GoogleConnection.create(UUID.randomUUID(), TENANT_ID, MANAGER_ID, CRED_PATH, Instant.now());
        when(repository.findByTenantId(TENANT_ID)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(conn);

        GoogleConnection result = service.createConnection(TENANT_ID, MANAGER_ID, CRED_PATH);

        assertThat(result).isNotNull();
        verify(repository).save(any());
        verify(eventBus).publish(any());
    }

    @Test
    void shouldThrowConflictWhenTenantAlreadyHasConnection() {
        GoogleConnection existing = GoogleConnection.create(UUID.randomUUID(), TENANT_ID, MANAGER_ID, CRED_PATH, Instant.now());
        when(repository.findByTenantId(TENANT_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createConnection(TENANT_ID, MANAGER_ID, CRED_PATH))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldRotateCredentialAndPublishEvent() {
        UUID connId = UUID.randomUUID();
        GoogleConnection conn = GoogleConnection.create(connId, TENANT_ID, MANAGER_ID, CRED_PATH, Instant.now());
        conn.clearEvents();
        when(repository.findById(connId)).thenReturn(Optional.of(conn));
        when(repository.save(any())).thenReturn(conn);

        service.rotateCredential(connId, "new/path.json");

        verify(repository).save(any());
        verify(eventBus).publish(any());
    }
}
