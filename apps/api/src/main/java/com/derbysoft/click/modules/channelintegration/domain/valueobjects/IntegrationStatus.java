package com.derbysoft.click.modules.channelintegration.domain.valueobjects;

public sealed interface IntegrationStatus
    permits IntegrationStatus.SetupRequired, IntegrationStatus.Active,
            IntegrationStatus.Paused, IntegrationStatus.Broken {

    record SetupRequired() implements IntegrationStatus {}
    record Active() implements IntegrationStatus {}
    record Paused() implements IntegrationStatus {}
    record Broken() implements IntegrationStatus {}

    SetupRequired SETUP_REQUIRED = new SetupRequired();
    Active ACTIVE = new Active();
    Paused PAUSED = new Paused();
    Broken BROKEN = new Broken();

    default String name() {
        return switch (this) {
            case SetupRequired ignored -> "SetupRequired";
            case Active ignored -> "Active";
            case Paused ignored -> "Paused";
            case Broken ignored -> "Broken";
        };
    }

    static IntegrationStatus fromString(String value) {
        return switch (value) {
            case "SetupRequired" -> SETUP_REQUIRED;
            case "Active" -> ACTIVE;
            case "Paused" -> PAUSED;
            case "Broken" -> BROKEN;
            default -> throw new IllegalArgumentException("Unknown IntegrationStatus: " + value);
        };
    }
}
