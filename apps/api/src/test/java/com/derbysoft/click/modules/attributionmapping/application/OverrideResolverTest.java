package com.derbysoft.click.modules.attributionmapping.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.derbysoft.click.modules.attributionmapping.application.services.OverrideResolver;
import com.derbysoft.click.modules.attributionmapping.domain.MappingOverrideRepository;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OverrideResolverTest {

    @Mock MappingOverrideRepository overrideRepository;

    private OverrideResolver resolver;

    private static final UUID TENANT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        resolver = new OverrideResolver(overrideRepository);
    }

    @Test
    void shouldReturnCampaignScopedOverride() {
        MappingOverrideEntity campaignOverride = new MappingOverrideEntity();
        when(overrideRepository.findMatchingOverride(TENANT_ID, "123", "campaign-1"))
            .thenReturn(Optional.of(campaignOverride));

        Optional<MappingOverrideEntity> result = resolver.resolve(TENANT_ID, "123", "campaign-1");

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(campaignOverride);
    }

    @Test
    void shouldReturnAccountScopedWhenNoCampaignMatch() {
        MappingOverrideEntity accountOverride = new MappingOverrideEntity();
        when(overrideRepository.findMatchingOverride(TENANT_ID, "123", "campaign-1"))
            .thenReturn(Optional.of(accountOverride));

        Optional<MappingOverrideEntity> result = resolver.resolve(TENANT_ID, "123", "campaign-1");

        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        when(overrideRepository.findMatchingOverride(TENANT_ID, "unknown", "any"))
            .thenReturn(Optional.empty());

        Optional<MappingOverrideEntity> result = resolver.resolve(TENANT_ID, "unknown", "any");

        assertThat(result).isEmpty();
    }
}
