package com.derbysoft.click.modules.channelintegration.domain.valueobjects;

public record SyncSchedule(String cron, String timezone) {
    public SyncSchedule {
        if (cron == null || cron.isBlank()) {
            throw new IllegalArgumentException("cron must not be blank");
        }
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("timezone must not be blank");
        }
    }
}
