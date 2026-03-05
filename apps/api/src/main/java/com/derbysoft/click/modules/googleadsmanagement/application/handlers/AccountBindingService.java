package com.derbysoft.click.modules.googleadsmanagement.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.domain.AccountBindingRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.ConnectionStatus;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountBindingService {

    private final AccountBindingRepository bindingRepository;
    private final GoogleConnectionRepository connectionRepository;
    private final InProcessEventBus eventBus;

    public AccountBindingService(
        AccountBindingRepository bindingRepository,
        GoogleConnectionRepository connectionRepository,
        InProcessEventBus eventBus
    ) {
        this.bindingRepository = bindingRepository;
        this.connectionRepository = connectionRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public AccountBinding createBinding(UUID connectionId, String customerId, BindingType bindingType) {
        GoogleConnection connection = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new DomainError.NotFound("BIND_100",
                "GoogleConnection not found: " + connectionId));

        if (connection.getStatus() != ConnectionStatus.ACTIVE) {
            throw new DomainError.Conflict("BIND_101",
                "Cannot create binding: connection is not ACTIVE; status: " + connection.getStatus());
        }

        bindingRepository.findByConnectionIdAndCustomerId(connectionId, customerId)
            .filter(b -> b.getStatus() != BindingStatus.REMOVED)
            .ifPresent(existing -> {
                throw new DomainError.Conflict("BIND_102",
                    "An active binding already exists for connection " + connectionId
                        + " and customer " + customerId);
            });

        UUID id = UUID.randomUUID();
        AccountBinding binding = AccountBinding.create(
            id, connectionId, connection.getTenantId(), customerId, bindingType, Instant.now()
        );
        AccountBinding saved = bindingRepository.save(binding);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public AccountBinding removeBinding(UUID bindingId) {
        AccountBinding binding = findById(bindingId);
        binding.remove();
        AccountBinding saved = bindingRepository.save(binding);
        publishAndClear(saved);
        return saved;
    }

    public List<AccountBinding> resolveApplicableAccounts(UUID tenantId) {
        return bindingRepository.findByTenantId(tenantId).stream()
            .filter(b -> b.getStatus() == BindingStatus.ACTIVE)
            .toList();
    }

    public AccountBinding findById(UUID bindingId) {
        return bindingRepository.findById(bindingId)
            .orElseThrow(() -> new DomainError.NotFound("BIND_404",
                "AccountBinding not found: " + bindingId));
    }

    public List<AccountBinding> findByConnectionId(UUID connectionId) {
        return bindingRepository.findByConnectionId(connectionId);
    }

    private void publishAndClear(AccountBinding binding) {
        binding.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        binding.clearEvents();
    }
}
