package com.derbysoft.click.modules.campaignexecution.domain.errors;

import java.time.Duration;

public class RateLimitExceededException extends RuntimeException {

    private final Duration retryAfter;

    public RateLimitExceededException(Duration retryAfter) {
        super("Manual execution rate limit exceeded. Retry after " + retryAfter.getSeconds() + "s.");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
