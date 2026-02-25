package api.application.dto;

public record CreatePropertyRequest(
    String name,
    boolean isActive,
    String externalPropertyRef
) {}
