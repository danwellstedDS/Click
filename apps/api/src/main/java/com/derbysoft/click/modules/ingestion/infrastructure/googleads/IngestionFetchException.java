package com.derbysoft.click.modules.ingestion.infrastructure.googleads;

import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;

public class IngestionFetchException extends RuntimeException {

    private final FailureClass failureClass;

    public IngestionFetchException(FailureClass failureClass, String message, Throwable cause) {
        super(message, cause);
        this.failureClass = failureClass;
    }

    public IngestionFetchException(FailureClass failureClass, String message) {
        super(message);
        this.failureClass = failureClass;
    }

    public FailureClass getFailureClass() {
        return failureClass;
    }
}
