package com.derbysoft.click.modules.googleadsmanagement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingCreated;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingRemoved;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingStaleFlagged;
import com.derbysoft.click.modules.googleadsmanagement.domain.events.BindingStaleRecovered;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountBindingTest {

    private static final UUID CONNECTION_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String CUSTOMER_ID = "506-204-8043";

    private static AccountBinding newBinding() {
        return AccountBinding.create(
            UUID.randomUUID(), CONNECTION_ID, TENANT_ID, CUSTOMER_ID, BindingType.OWNED, Instant.now()
        );
    }

    @Test
    void shouldCreateBindingInActiveStatus() {
        AccountBinding binding = newBinding();
        assertThat(binding.getStatus()).isEqualTo(BindingStatus.ACTIVE);
        assertThat(binding.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(binding.getBindingType()).isEqualTo(BindingType.OWNED);
    }

    @Test
    void shouldEmitBindingCreatedOnCreate() {
        AccountBinding binding = newBinding();
        assertThat(binding.getEvents()).hasSize(1);
        assertThat(binding.getEvents().get(0)).isInstanceOf(BindingCreated.class);
    }

    @Test
    void shouldRemoveAndEmitBindingRemovedEvent() {
        AccountBinding binding = newBinding();
        binding.clearEvents();
        binding.remove();
        assertThat(binding.getStatus()).isEqualTo(BindingStatus.REMOVED);
        assertThat(binding.getEvents()).hasSize(1);
        assertThat(binding.getEvents().get(0)).isInstanceOf(BindingRemoved.class);
    }

    @Test
    void shouldFlagStaleAndEmitEvent() {
        AccountBinding binding = newBinding();
        binding.clearEvents();
        binding.flagStale();
        assertThat(binding.getStatus()).isEqualTo(BindingStatus.STALE);
        assertThat(binding.getEvents()).hasSize(1);
        assertThat(binding.getEvents().get(0)).isInstanceOf(BindingStaleFlagged.class);
    }

    @Test
    void shouldRecoverFromStaleAndEmitEvent() {
        AccountBinding binding = newBinding();
        binding.flagStale();
        binding.clearEvents();
        binding.recoverFromStale();
        assertThat(binding.getStatus()).isEqualTo(BindingStatus.ACTIVE);
        assertThat(binding.getEvents()).hasSize(1);
        assertThat(binding.getEvents().get(0)).isInstanceOf(BindingStaleRecovered.class);
    }

    @Test
    void shouldThrowWhenRemovingAlreadyRemovedBinding() {
        AccountBinding binding = newBinding();
        binding.remove();
        assertThatThrownBy(binding::remove)
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("already REMOVED");
    }
}
