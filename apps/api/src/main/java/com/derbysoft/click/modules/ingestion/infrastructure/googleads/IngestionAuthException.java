package com.derbysoft.click.modules.ingestion.infrastructure.googleads;

public class IngestionAuthException extends RuntimeException {

    public IngestionAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public IngestionAuthException(String message) {
        super(message);
    }
}
