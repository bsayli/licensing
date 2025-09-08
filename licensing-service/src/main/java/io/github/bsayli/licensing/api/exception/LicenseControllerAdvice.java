package io.github.bsayli.licensing.api.exception;

import io.github.bsayli.licensing.api.dto.ApiError;
import io.github.bsayli.licensing.api.dto.ApiResponse;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;
import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.repository.exception.RepositoryException;
import io.github.bsayli.licensing.service.user.operations.exception.UserOperationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LicenseControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(LicenseControllerAdvice.class);
  private final LocalizedMessageResolver messageResolver;

  public LicenseControllerAdvice(LocalizedMessageResolver messageResolver) {
    this.messageResolver = messageResolver;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgNotValid(
      MethodArgumentNotValidException ex) {
    int fieldErrorCount = ex.getBindingResult().getFieldErrorCount();
    log.atWarn()
        .setCause(ex)
        .addArgument(fieldErrorCount)
        .log("Request validation failed (MethodArgumentNotValidException). fieldErrors={} ");

    Locale locale = LocaleContextHolder.getLocale();
    List<ApiError> errors = new ArrayList<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      String keyOrText = fe.getDefaultMessage();
      String resolved = resolveMessageKeyOrText(keyOrText, locale);
      errors.add(
          new ApiError(ServiceErrorCode.INVALID_PARAMETER.name(), fe.getField() + ": " + resolved));
    }

    String topMsg = messageResolver.getMessage("request.validation.failed", locale);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, topMsg, errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
      ConstraintViolationException ex) {
    int violationCount =
        ex.getConstraintViolations() != null ? ex.getConstraintViolations().size() : 0;
    log.atWarn()
        .setCause(ex)
        .addArgument(violationCount)
        .log("Constraint violation on request parameters. violations={} ");

    Locale locale = LocaleContextHolder.getLocale();
    List<ApiError> errors = new ArrayList<>();
    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      String keyOrText = v.getMessageTemplate();
      String resolved = resolveMessageKeyOrText(keyOrText, locale, v.getMessage());
      String path = v.getPropertyPath().toString().replace(".validatedValue", "");
      errors.add(new ApiError(ServiceErrorCode.INVALID_PARAMETER.name(), path + ": " + resolved));
    }

    String topMsg = messageResolver.getMessage("request.validation.failed", locale);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, topMsg, errors));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
      MissingServletRequestParameterException ex) {
    log.atWarn()
        .setCause(ex)
        .addArgument(ex.getParameterName())
        .log("Missing request parameter: {}");

    Locale locale = LocaleContextHolder.getLocale();
    String msg = messageResolver.getMessage("request.param.missing", locale, ex.getParameterName());

    List<ApiError> errors = List.of(new ApiError(ServiceErrorCode.MISSING_PARAMETER.name(), msg));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg, errors));
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
    log.atWarn().setCause(ex).addArgument(ex.getHeaderName()).log("Missing request header: {}");

    Locale locale = LocaleContextHolder.getLocale();
    String msg = messageResolver.getMessage("request.header.missing", locale, ex.getHeaderName());

    List<ApiError> errors = List.of(new ApiError(ServiceErrorCode.MISSING_PARAMETER.name(), msg));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(HttpStatus.BAD_REQUEST, msg, errors));
  }

  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
    ServiceErrorCode code = ex.getCode();
    HttpStatus http = mapCodeToHttp(code);

    var logger = http.is5xxServerError() ? log.atError() : log.atWarn();
    logger
        .setCause(ex)
        .addArgument(http.value())
        .addArgument(code)
        .addArgument(ex.getMessageKey())
        .log("Service exception. statusCode={}, code={}, messageKey={}");

    Locale locale = LocaleContextHolder.getLocale();
    String detail = messageResolver.getMessage(ex.getMessageKey(), locale, ex.getArgs());
    String top = messageResolver.getMessage("license.validation.failed", locale);

    ApiResponse<Void> body =
        ApiResponse.error(http, top, List.of(new ApiError(code.name(), detail)));

    return ResponseEntity.status(http).body(body);
  }

  @ExceptionHandler(RepositoryException.class)
  public ResponseEntity<ApiResponse<Void>> handleRepositoryException(RepositoryException ex) {
    log.atWarn()
        .setCause(ex)
        .addArgument(ex.getErrorCode())
        .addArgument(ex.getMessageKey())
        .log("Repository exception. errorCode={}, messageKey={} ");

    Locale locale = LocaleContextHolder.getLocale();

    String detail = messageResolver.getMessage(ex.getMessageKey(), locale, ex.getMessageArgs());
    String top = messageResolver.getMessage("repository.operation.failed", locale);

    HttpStatus http =
        switch (ex.getErrorCode()) {
          case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
          case USER_ATTRIBUTE_MISSING, USER_ATTRIBUTE_INVALID_FORMAT -> HttpStatus.BAD_REQUEST;
        };

    ApiResponse<Void> body =
        ApiResponse.error(http, top, List.of(new ApiError(ex.getErrorCode().name(), detail)));

    return ResponseEntity.status(http).body(body);
  }

  @ExceptionHandler(UserOperationException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserOperationException(UserOperationException ex) {
    log.atWarn()
        .setCause(ex)
        .addArgument(ex.getErrorCode())
        .addArgument(ex.getMessageKey())
        .log("User operation exception. errorCode={}, messageKey={} ");

    Locale locale = LocaleContextHolder.getLocale();

    String detail = messageResolver.getMessage(ex.getMessageKey(), locale, ex.getMessageArgs());

    String top = messageResolver.getMessage("user.operation.failed", locale);

    HttpStatus http =
        switch (ex.getErrorCode()) {
          case ALREADY_PROCESSING -> HttpStatus.CONFLICT;
          case MAX_RETRY_ATTEMPTS_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
        };

    ApiResponse<Void> body =
        ApiResponse.error(http, top, List.of(new ApiError(ex.getErrorCode().name(), detail)));

    return ResponseEntity.status(http).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    log.atError().setCause(ex).log("Unexpected error in License API");

    Locale locale = LocaleContextHolder.getLocale();
    String msg = messageResolver.getMessage("license.validation.error", locale);

    List<ApiError> errors =
        List.of(new ApiError(ServiceErrorCode.INTERNAL_SERVER_ERROR.name(), msg));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, msg, errors));
  }

  private HttpStatus mapCodeToHttp(ServiceErrorCode s) {
    return switch (s) {
      case LICENSE_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case LICENSE_INVALID,
          LICENSE_EXPIRED,
          LICENSE_INACTIVE,
          LICENSE_USAGE_LIMIT_EXCEEDED,
          TOKEN_INVALID,
          TOKEN_EXPIRED,
          TOKEN_IS_TOO_OLD_FOR_REFRESH,
          SIGNATURE_INVALID,
          LICENSE_SERVICE_ID_NOT_SUPPORTED,
          LICENSE_INVALID_SERVICE_ID,
          LICENSE_INVALID_CHECKSUM,
          LICENSE_SERVICE_VERSION_NOT_SUPPORTED ->
          HttpStatus.UNAUTHORIZED;
      case TOKEN_INVALID_ACCESS -> HttpStatus.FORBIDDEN;
      case TOKEN_ALREADY_EXIST -> HttpStatus.CONFLICT;
      case INVALID_PARAMETER, MISSING_PARAMETER, INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
      case INTERNAL_SERVER_ERROR, UNKNOWN_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
      case TOKEN_CREATED, TOKEN_REFRESHED, TOKEN_ACTIVE -> HttpStatus.OK;
    };
  }

  private String resolveMessageKeyOrText(String maybeKey, Locale locale) {
    return resolveMessageKeyOrText(maybeKey, locale, maybeKey);
  }

  private String resolveMessageKeyOrText(String maybeKey, Locale locale, String fallback) {
    if (maybeKey == null) return fallback;
    String key = maybeKey;
    if (key.startsWith("{") && key.endsWith("}")) {
      key = key.substring(1, key.length() - 1);
    } else {
      return maybeKey;
    }
    return messageResolver.getMessage(key, locale);
  }
}
