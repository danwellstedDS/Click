package com.derbysoft.click.modules.normalisation.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MappingVersionTest {

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> new MappingVersion(""))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new MappingVersion("  "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void v1ConstantShouldHaveValueV1() {
        assertThat(MappingVersion.V1.value()).isEqualTo("v1");
    }
}
