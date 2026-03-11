package com.derbysoft.click.modules.normalisation.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.derbysoft.click.modules.normalisation.application.services.BatchAssembler;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchAssemblerTest {

    private BatchAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new BatchAssembler();
    }

    private CanonicalFactEntity factWithKey(String key) {
        CanonicalFactEntity entity = new CanonicalFactEntity();
        entity.setId(UUID.randomUUID());
        entity.setReconciliationKey(key);
        return entity;
    }

    @Test
    void shouldProduceDeterministicChecksum() {
        List<CanonicalFactEntity> facts = List.of(factWithKey("key-a"), factWithKey("key-b"));

        String checksum1 = assembler.computeChecksum(facts);
        String checksum2 = assembler.computeChecksum(facts);

        assertThat(checksum1).isEqualTo(checksum2);
        assertThat(checksum1).hasSize(64);
        assertThat(checksum1).matches("[0-9a-f]+");
    }

    @Test
    void shouldBeOrderIndependent() {
        CanonicalFactEntity factA = factWithKey("key-a");
        CanonicalFactEntity factB = factWithKey("key-b");

        String checksum1 = assembler.computeChecksum(List.of(factA, factB));
        String checksum2 = assembler.computeChecksum(List.of(factB, factA));

        assertThat(checksum1).isEqualTo(checksum2);
    }

    @Test
    void shouldProduceDifferentChecksumForDifferentFactSets() {
        String checksumA = assembler.computeChecksum(List.of(factWithKey("key-a")));
        String checksumB = assembler.computeChecksum(List.of(factWithKey("key-b")));

        assertThat(checksumA).isNotEqualTo(checksumB);
    }

    @Test
    void shouldHandleEmptyList() {
        String checksum = assembler.computeChecksum(List.of());

        assertThat(checksum).hasSize(64);
        assertThat(checksum).matches("[0-9a-f]+");
    }
}
