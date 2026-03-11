package com.derbysoft.click.modules.normalisation.application.services;

import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BatchAssembler {

    private static final Logger log = LoggerFactory.getLogger(BatchAssembler.class);

    public String computeChecksum(List<CanonicalFactEntity> facts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            facts.stream()
                .sorted(Comparator.comparing(CanonicalFactEntity::getReconciliationKey))
                .forEach(f -> digest.update(f.getReconciliationKey().getBytes(StandardCharsets.UTF_8)));
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.warn("Failed to compute batch checksum: {}", e.getMessage());
            return "unknown";
        }
    }
}
