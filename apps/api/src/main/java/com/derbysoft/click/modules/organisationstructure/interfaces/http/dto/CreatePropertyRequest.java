package com.derbysoft.click.modules.organisationstructure.interfaces.http.dto;

public record CreatePropertyRequest(
    String name,
    boolean isActive,
    String externalPropertyRef
) {}
