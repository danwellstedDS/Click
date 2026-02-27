package com.derbysoft.click.bootstrap.web;

import com.derbysoft.click.sharedkernel.api.ApiResponse;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandlingAdvice {

  @ExceptionHandler(DomainError.class)
  public ResponseEntity<ApiResponse<Object>> handleDomainError(
      DomainError error, HttpServletRequest request) {
    HttpStatus status = switch (error) {
      case DomainError.ValidationError ignored -> HttpStatus.BAD_REQUEST;
      case DomainError.NotFound ignored -> HttpStatus.NOT_FOUND;
      case DomainError.Conflict ignored -> HttpStatus.CONFLICT;
      case DomainError.Unauthenticated ignored -> HttpStatus.UNAUTHORIZED;
      case DomainError.Forbidden ignored -> HttpStatus.FORBIDDEN;
    };
    return new ResponseEntity<>(
        ApiResponse.error(error.getCode(), error.getMessage(), requestId(request)),
        status
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleUnexpected(
      Exception error, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("SYS_001", "Unexpected error", requestId(request)));
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestContextFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
