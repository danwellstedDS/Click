package com.derbysoft.click.modules.attributionmapping.application.services;

import com.derbysoft.click.modules.attributionmapping.domain.MappingOverrideRepository;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OverrideSetVersionComputer {

    private final MappingOverrideRepository overrideRepository;

    public OverrideSetVersionComputer(MappingOverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    /**
     * Computes a SHA-256 hash of sorted active override IDs for a tenant.
     * Stable across replays as long as the override set has not changed.
     */
    public String compute(UUID tenantId) {
        List<MappingOverrideEntity> active = overrideRepository.findActiveByTenantId(tenantId);
        if (active.isEmpty()) {
            return "empty";
        }
        String joined = active.stream()
            .map(MappingOverrideEntity::getId)
            .sorted(Comparator.comparing(UUID::toString))
            .map(UUID::toString)
            .reduce("", (a, b) -> a + "," + b);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
