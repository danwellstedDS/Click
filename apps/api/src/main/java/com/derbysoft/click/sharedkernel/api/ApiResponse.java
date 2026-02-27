package com.derbysoft.click.sharedkernel.api;

public record ApiResponse<T>(boolean success, T data, ErrorDetail error, Meta meta) {

  public static <T> ApiResponse<T> success(T data, String requestId) {
    return new ApiResponse<>(true, data, null, new Meta(requestId));
  }

  public static <T> ApiResponse<T> error(String code, String message, String requestId) {
    return new ApiResponse<>(false, null, new ErrorDetail(code, message), new Meta(requestId));
  }

  public record ErrorDetail(String code, String message) {}

  public record Meta(String requestId) {}
}
