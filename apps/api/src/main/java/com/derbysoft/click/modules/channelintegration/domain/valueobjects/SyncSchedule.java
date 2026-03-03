package com.derbysoft.click.modules.channelintegration.domain.valueobjects;

public record SyncSchedule(
    CadenceType cadenceType,
    String cronExpression,
    Integer intervalMinutes,
    String timezone
) {
    public SyncSchedule {
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("timezone must not be blank");
        }
        switch (cadenceType) {
            case CRON -> {
                if (cronExpression == null || cronExpression.isBlank()) {
                    throw new IllegalArgumentException("cronExpression must not be blank for CRON cadence");
                }
                if (intervalMinutes != null) {
                    throw new IllegalArgumentException("intervalMinutes must be null for CRON cadence");
                }
            }
            case INTERVAL -> {
                if (intervalMinutes == null || intervalMinutes < 5) {
                    throw new IllegalArgumentException("intervalMinutes must be >= 5 for INTERVAL cadence");
                }
                if (cronExpression != null) {
                    throw new IllegalArgumentException("cronExpression must be null for INTERVAL cadence");
                }
            }
            case MANUAL -> {
                if (cronExpression != null) {
                    throw new IllegalArgumentException("cronExpression must be null for MANUAL cadence");
                }
                if (intervalMinutes != null) {
                    throw new IllegalArgumentException("intervalMinutes must be null for MANUAL cadence");
                }
            }
        }
    }

    public static SyncSchedule manual(String timezone) {
        return new SyncSchedule(CadenceType.MANUAL, null, null, timezone);
    }

    public static SyncSchedule cron(String cronExpression, String timezone) {
        return new SyncSchedule(CadenceType.CRON, cronExpression, null, timezone);
    }

    public static SyncSchedule interval(int intervalMinutes, String timezone) {
        return new SyncSchedule(CadenceType.INTERVAL, null, intervalMinutes, timezone);
    }
}
