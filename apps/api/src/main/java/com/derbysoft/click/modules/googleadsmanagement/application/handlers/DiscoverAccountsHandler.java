package com.derbysoft.click.modules.googleadsmanagement.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort.DiscoveredAccount;
import com.derbysoft.click.modules.googleadsmanagement.domain.AccountBindingRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.entities.AccountGraphState;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.AccountsDiscovered;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.ConnectionBroken;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.DiscoveryFailed;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.ConnectionStatus;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountGraphStateRepository;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscoverAccountsHandler {

    private final GoogleConnectionRepository connectionRepository;
    private final AccountBindingRepository bindingRepository;
    private final AccountGraphStateRepository graphStateRepository;
    private final InProcessEventBus eventBus;
    private final GoogleAdsApiPort googleAdsApiPort;

    public DiscoverAccountsHandler(
        GoogleConnectionRepository connectionRepository,
        AccountBindingRepository bindingRepository,
        AccountGraphStateRepository graphStateRepository,
        InProcessEventBus eventBus,
        GoogleAdsApiPort googleAdsApiPort
    ) {
        this.connectionRepository = connectionRepository;
        this.bindingRepository = bindingRepository;
        this.graphStateRepository = graphStateRepository;
        this.eventBus = eventBus;
        this.googleAdsApiPort = googleAdsApiPort;
    }

    @Transactional
    public void discover(UUID connectionId) {
        GoogleConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new DomainError.NotFound("DISC_404",
                "GoogleConnection not found: " + connectionId));

        if (connection.getStatus() != ConnectionStatus.ACTIVE) {
            throw new DomainError.Conflict("DISC_001",
                "Cannot discover accounts: connection is not ACTIVE; status: " + connection.getStatus());
        }

        try {
            List<DiscoveredAccount> discovered = googleAdsApiPort.listAccessibleAccounts(
                connection.getManagerId(), connection.getCredentialPath()
            );

            Instant now = Instant.now();

            // Replace account graph state
            graphStateRepository.deleteByConnectionId(connectionId);
            List<AccountGraphState> newStates = discovered.stream()
                .map(acc -> new AccountGraphState(
                    UUID.randomUUID(), connectionId, acc.customerId(),
                    acc.name(), acc.currencyCode(), acc.timeZone(), now
                ))
                .toList();
            newStates.forEach(graphStateRepository::save);

            Set<String> discoveredCustomerIds = discovered.stream()
                .map(DiscoveredAccount::customerId)
                .collect(Collectors.toSet());

            List<AccountBinding> bindings = bindingRepository.findByConnectionId(connectionId);

            // Flag stale: ACTIVE bindings whose customerId is not in the discovered set
            bindings.stream()
                .filter(b -> b.getStatus() == BindingStatus.ACTIVE
                    && !discoveredCustomerIds.contains(b.getCustomerId()))
                .forEach(b -> {
                    b.flagStale();
                    AccountBinding saved = bindingRepository.save(b);
                    publishAndClearBinding(saved);
                });

            // Recover stale: STALE bindings whose customerId is back in the discovered set
            bindings.stream()
                .filter(b -> b.getStatus() == BindingStatus.STALE
                    && discoveredCustomerIds.contains(b.getCustomerId()))
                .forEach(b -> {
                    b.recoverFromStale();
                    AccountBinding saved = bindingRepository.save(b);
                    publishAndClearBinding(saved);
                });

            connection.recordDiscovery(now);
            connectionRepository.save(connection);

            eventBus.publish(EventEnvelope.of("AccountsDiscovered",
                new AccountsDiscovered(connectionId, connection.getTenantId(), discovered.size(), now)));

        } catch (DomainError e) {
            throw e;
        } catch (Exception e) {
            String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            connection.markBroken(reason);
            connectionRepository.save(connection);
            Instant now = Instant.now();
            eventBus.publish(EventEnvelope.of("ConnectionBroken",
                new ConnectionBroken(connectionId, reason, now)));
            eventBus.publish(EventEnvelope.of("DiscoveryFailed",
                new DiscoveryFailed(connectionId, reason, now)));
        }
    }

    private void publishAndClearBinding(AccountBinding binding) {
        binding.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        binding.clearEvents();
    }
}
