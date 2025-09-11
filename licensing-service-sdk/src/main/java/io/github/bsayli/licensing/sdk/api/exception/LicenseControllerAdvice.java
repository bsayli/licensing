package io.github.bsayli.licensing.sdk.api.exception;

import io.github.bsayli.licensing.sdk.common.api.ApiError;
import io.github.bsayli.licensing.sdk.common.api.ApiResponse;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkHttpTransportException;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.sdk.common.i18n.LocalizedMessageResolver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LicenseControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(LicenseControllerAdvice.class);
  private final LocalizedMessageResolver messages;

  public LicenseControllerAdvice(LocalizedMessageResolver messages) {
    this.messages = messages;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
    List<ApiError> errors = new ArrayList<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      String keyOrText = fe.getDefaultMessage();
      String resolved = resolveStrict(keyOrText);
      errors.add(new ApiError("INVALID_PARAMETER", fe.getField() + ": " + resolved));
    }
    String topMsg = messages.getMessage("request.validation.failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST, topMsg, errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
    List<ApiError> errors = new ArrayList<>();
    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      String template = v.getMessageTemplate();
      String resolved = resolveStrict(template, v.getMessage());
      String path = v.getPropertyPath().toString().replace(".validatedValue", "");
      errors.add(new ApiError("INVALID_PARAMETER", path + ": " + resolved));
    }
    String topMsg = messages.getMessage("request.validation.failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST, topMsg, errors));
  }

  @ExceptionHandler(LicensingSdkRemoteServiceException.class)
  public ResponseEntity<ApiResponse<Void>> handleRemoteError(LicensingSdkRemoteServiceException ex) {
    HttpStatus http = Objects.requireNonNullElse(ex.getHttpStatus(), HttpStatus.INTERNAL_SERVER_ERROR);

    String top = (ex.getTopMessage() != null && !ex.getTopMessage().isBlank())
            ? ex.getTopMessage()
            : messages.getMessage("license.validation.failed");

    List<ApiError> errs;
    if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
      String code = (ex.getErrorCode() != null && !ex.getErrorCode().isBlank())
              ? ex.getErrorCode()
              : "REMOTE_ERROR";
      errs = ex.getDetails().stream()
              .map(d -> new ApiError(code, d))
              .collect(Collectors.toList());
    } else if (ex.getErrorCode() != null && !ex.getErrorCode().isBlank()) {
      errs = List.of(new ApiError(ex.getErrorCode(), top));
    } else {
      errs = List.of();
    }

    return ResponseEntity.status(http).body(ApiResponse.error(http, top, errs));
  }

  @ExceptionHandler(LicensingSdkHttpTransportException.class)
  public ResponseEntity<ApiResponse<Void>> handleTransportError(LicensingSdkHttpTransportException ex) {
    log.error("Transport/parse error when calling licensing-service", ex);
    HttpStatus http = HttpStatus.BAD_GATEWAY;
    String top = messages.getMessage("license.validation.error");
    List<ApiError> errs = List.of(new ApiError("TRANSPORT_ERROR", top));
    return ResponseEntity.status(http).body(ApiResponse.error(http, top, errs));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
    log.error("Unexpected error in SDK API layer", ex);
    HttpStatus http = HttpStatus.INTERNAL_SERVER_ERROR;
    String top = messages.getMessage("license.validation.error");
    List<ApiError> errs = List.of(new ApiError("INTERNAL_SERVER_ERROR", top));
    return ResponseEntity.status(http).body(ApiResponse.error(http, top, errs));
  }

  private String resolveStrict(String keyOrText, Object... args) {
    if (keyOrText == null) return null;
    String s = keyOrText.trim();
    if (s.startsWith("{") && s.endsWith("}")) {
      String key = s.substring(1, s.length() - 1);
      return (args == null || args.length == 0)
              ? messages.getMessage(key)
              : messages.getMessage(key, args);
    }
    return keyOrText;
  }
}