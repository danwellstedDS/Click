package com.derbysoft.click.modules.googleadsmanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.application.handlers.AccountBindingService;
import com.derbysoft.click.modules.googleadsmanagement.domain.AccountBindingRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountBindingServiceTest {

    @Mock
    private AccountBindingRepository bindingRepository;

    @Mock
    private GoogleConnectionRepository connectionRepository;

    @Mock
    private InProcessEventBus eventBus;

    @InjectMocks
    private AccountBindingService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID CONNECTION_ID = UUID.randomUUID();
    private static final String CUSTOMER_ID = "506-204-8043";

    private GoogleConnection activeConnection() {
        return GoogleConnection.create(CONNECTION_ID, TENANT_ID, "858-270-7576",
            "infra/secrets/creds.json", Instant.now());
    }

    private AccountBinding activeBinding() {
        return AccountBinding.create(UUID.randomUUID(), CONNECTION_ID, TENANT_ID,
            CUSTOMER_ID, BindingType.OWNED, Instant.now());
    }

    @Test
    void shouldCreateBindingAndPublishEvent() {
        AccountBinding binding = activeBinding();
        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(activeConnection()));
        when(bindingRepository.findByConnectionIdAndCustomerId(CONNECTION_ID, CUSTOMER_ID))
            .thenReturn(Optional.empty());
        when(bindingRepository.save(any())).thenReturn(binding);

        AccountBinding result = service.createBinding(CONNECTION_ID, CUSTOMER_ID, BindingType.OWNED);

        assertThat(result).isNotNull();
        verify(bindingRepository).save(any());
        verify(eventBus).publish(any());
    }

    @Test
    void shouldThrowConflictWhenDuplicateBinding() {
        AccountBinding existing = activeBinding();
        when(connectionRepository.findById(CONNECTION_ID)).thenReturn(Optional.of(activeConnection()));
        when(bindingRepository.findByConnectionIdAndCustomerId(CONNECTION_ID, CUSTOMER_ID))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createBinding(CONNECTION_ID, CUSTOMER_ID, BindingType.OWNED))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("active binding already exists");
    }

    @Test
    void shouldRemoveBindingAndPublishEvent() {
        UUID bindingId = UUID.randomUUID();
        AccountBinding binding = AccountBinding.create(bindingId, CONNECTION_ID, TENANT_ID,
            CUSTOMER_ID, BindingType.OWNED, Instant.now());
        binding.clearEvents();
        when(bindingRepository.findById(bindingId)).thenReturn(Optional.of(binding));
        when(bindingRepository.save(any())).thenReturn(binding);

        service.removeBinding(bindingId);

        verify(bindingRepository).save(any());
        verify(eventBus).publish(any());
    }

    @Test
    void shouldResolveOnlyActiveBindingsForTenant() {
        AccountBinding active = AccountBinding.create(UUID.randomUUID(), CONNECTION_ID, TENANT_ID,
            "111-111-1111", BindingType.OWNED, Instant.now());
        AccountBinding removed = AccountBinding.reconstitute(UUID.randomUUID(), CONNECTION_ID, TENANT_ID,
            "222-222-2222", BindingStatus.REMOVED, BindingType.OWNED, Instant.now(), Instant.now());
        when(bindingRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(active, removed));

        List<AccountBinding> result = service.resolveApplicableAccounts(TENANT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BindingStatus.ACTIVE);
    }
}
