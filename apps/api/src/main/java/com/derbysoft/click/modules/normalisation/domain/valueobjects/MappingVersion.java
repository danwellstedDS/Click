package com.derbysoft.click.modules.normalisation.domain.valueobjects;

public record MappingVersion(String value) {
    public static final MappingVersion V1 = new MappingVersion("v1");

    public MappingVersion {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("MappingVersion must not be blank");
    }
}
