package api.presentation;

import api.ApiResponse;
import api.application.AuthException;
import api.security.RequestIdFilter;
import domain.error.DomainError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(DomainError.class)
  public ResponseEntity<ApiResponse<Object>> handleDomainError(DomainError error, HttpServletRequest request) {
    HttpStatus status = switch (error) {
      case DomainError.ValidationError ignored -> HttpStatus.BAD_REQUEST;
      case DomainError.NotFound ignored -> HttpStatus.NOT_FOUND;
      case DomainError.Conflict ignored -> HttpStatus.CONFLICT;
    };
    return new ResponseEntity<>(ApiResponse.error(error.getCode(), error.getMessage(), requestId(request)), status);
  }

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ApiResponse<Object>> handleAuthError(AuthException error, HttpServletRequest request) {
    return ResponseEntity.status(error.getStatus())
        .body(ApiResponse.error(error.getCode(), error.getMessage(), requestId(request)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception error, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("SYS_001", "Unexpected error", requestId(request)));
  }

  private static String requestId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTR);
    return value == null ? "unknown" : value.toString();
  }
}
