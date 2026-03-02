package com.derbysoft.click.modules.channelintegration.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by BC7 (Ingestion) when a sync run completes successfully.
 * BC4 listens for this event to update the integration instance's health status.
 * This event class is defined here as a stub until BC7 is implemented;
 * it will move to BC7's api/events package at that time.
 */
public record SyncSucceeded(
    UUID integrationId,
    UUID syncRunId,
    Instant occurredAt
) {}
