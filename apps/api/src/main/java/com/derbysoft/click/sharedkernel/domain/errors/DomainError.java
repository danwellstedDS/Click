package com.derbysoft.click.sharedkernel.domain.errors;

public sealed abstract class DomainError extends RuntimeException
    permits DomainError.NotFound, DomainError.ValidationError,
            DomainError.Conflict, DomainError.Unauthenticated,
            DomainError.Forbidden {

  private final String code;

  protected DomainError(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static final class NotFound extends DomainError {
    public NotFound(String code, String message) {
      super(code, message);
    }
  }

  public static final class ValidationError extends DomainError {
    public ValidationError(String code, String message) {
      super(code, message);
    }
  }

  public static final class Conflict extends DomainError {
    public Conflict(String code, String message) {
      super(code, message);
    }
  }

  public static final class Unauthenticated extends DomainError {
    public Unauthenticated(String code, String message) {
      super(code, message);
    }
  }

  public static final class Forbidden extends DomainError {
    public Forbidden(String code, String message) {
      super(code, message);
    }
  }
}
