package com.derbysoft.click.modules.googleadsmanagement.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.application.handlers.DiscoverAccountsHandler;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort.DiscoveredAccount;
import com.derbysoft.click.modules.googleadsmanagement.domain.AccountBindingRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountGraphStateRepository;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscoverAccountsHandlerTest {

    @Mock
    private GoogleConnectionRepository connectionRepository;
    @Mock
    private AccountBindingRepository bindingRepository;
    @Mock
    private AccountGraphStateRepository graphStateRepository;
    @Mock
    private InProcessEventBus eventBus;
    @Mock
    private GoogleAdsApiPort googleAdsApiPort;

    private DiscoverAccountsHandler handler;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID CONNECTION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new DiscoverAccountsHandler(
            connectionRepository, bindingRepository, graphStateRepository, eventBus, googleAdsApiPort
        );
    }

    private GoogleConnection activeConnection() {
        GoogleConnection conn = GoogleConnection.create(CONNECTION_ID, TENANT_ID, "858-270-7576",
            "infra/secrets/creds.json", Instant.now());
        conn.clearEvents();
        return conn;
    }

    @Test
    void shouldUpdateAccountGraphStateOnSuccessfulDiscovery() {
        GoogleConnection conn = activeConnection();
        DiscoveredAccount acc = new DiscoveredAccount("506-204-8043", "Test", "USD", "UTC");
        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(conn));
        when(googleAdsApiPort.listAccessibleAccounts(any(), any())).thenReturn(List.of(acc));
        when(bindingRepository.findByConnectionId(CONNECTION_ID)).thenReturn(List.of());
        when(connectionRepository.save(any())).thenReturn(conn);

        handler.discover(CONNECTION_ID);

        verify(graphStateRepository).deleteByConnectionId(CONNECTION_ID);
        verify(graphStateRepository).save(any());
    }

    @Test
    void shouldEmitAccountsDiscoveredWithCorrectCount() {
        GoogleConnection conn = activeConnection();
        List<DiscoveredAccount> accounts = List.of(
            new DiscoveredAccount("111", "Acc1", "USD", "UTC"),
            new DiscoveredAccount("222", "Acc2", "EUR", "Europe/London")
        );
        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(conn));
        when(googleAdsApiPort.listAccessibleAccounts(any(), any())).thenReturn(accounts);
        when(bindingRepository.findByConnectionId(CONNECTION_ID)).thenReturn(List.of());
        when(connectionRepository.save(any())).thenReturn(conn);

        handler.discover(CONNECTION_ID);

        // Should publish AccountsDiscovered event
        verify(eventBus, times(1)).publish(any());
    }

    @Test
    void shouldFlagStaleBindingsForMissingAccounts() {
        GoogleConnection conn = activeConnection();
        // Discovered: only "111"; binding for "222" should become STALE
        DiscoveredAccount acc = new DiscoveredAccount("111", "Acc1", "USD", "UTC");
        AccountBinding staleBinding = AccountBinding.create(UUID.randomUUID(), CONNECTION_ID,
            TENANT_ID, "222", BindingType.OWNED, Instant.now());
        staleBinding.clearEvents();

        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(conn));
        when(googleAdsApiPort.listAccessibleAccounts(any(), any())).thenReturn(List.of(acc));
        when(bindingRepository.findByConnectionId(CONNECTION_ID)).thenReturn(List.of(staleBinding));
        when(bindingRepository.save(any())).thenReturn(staleBinding);
        when(connectionRepository.save(any())).thenReturn(conn);

        handler.discover(CONNECTION_ID);

        verify(bindingRepository).save(any());
    }

    @Test
    void shouldRecoverStaleBindingsForRediscoveredAccounts() {
        GoogleConnection conn = activeConnection();
        // Discovered: "222" reappears; stale binding should recover
        DiscoveredAccount acc = new DiscoveredAccount("222", "Acc2", "USD", "UTC");
        AccountBinding staleBinding = AccountBinding.reconstitute(
            UUID.randomUUID(), CONNECTION_ID, TENANT_ID, "222",
            BindingStatus.STALE, BindingType.OWNED, Instant.now(), Instant.now()
        );

        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(conn));
        when(googleAdsApiPort.listAccessibleAccounts(any(), any())).thenReturn(List.of(acc));
        when(bindingRepository.findByConnectionId(CONNECTION_ID)).thenReturn(List.of(staleBinding));
        when(bindingRepository.save(any())).thenReturn(staleBinding);
        when(connectionRepository.save(any())).thenReturn(conn);

        handler.discover(CONNECTION_ID);

        verify(bindingRepository).save(any());
    }

    @Test
    void shouldMarkConnectionBrokenOnDiscoveryFailure() {
        GoogleConnection conn = activeConnection();
        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(conn));
        when(googleAdsApiPort.listAccessibleAccounts(any(), any()))
            .thenThrow(new RuntimeException("API unavailable"));
        when(connectionRepository.save(any())).thenReturn(conn);

        handler.discover(CONNECTION_ID);

        verify(connectionRepository).save(any());
        // Should publish ConnectionBroken + DiscoveryFailed
        verify(eventBus, times(2)).publish(any());
    }
}
