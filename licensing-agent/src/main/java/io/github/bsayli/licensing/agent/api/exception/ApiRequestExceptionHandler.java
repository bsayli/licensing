package io.github.bsayli.licensing.agent.api.exception;

import static io.github.bsayli.licensing.agent.common.api.ApiConstants.ErrorCode.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.github.blueprintplatform.openapi.generics.contract.error.ErrorItem;
import io.github.bsayli.licensing.agent.api.dto.LicenseAgentErrorResponse;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Order(1)
public class ApiRequestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestExceptionHandler.class);

    private static final String KEY_PROBLEM_TITLE_NOT_FOUND = "problem.title.not_found";
    private static final String KEY_PROBLEM_DETAIL_NOT_FOUND = "problem.detail.not_found";

    private static final String KEY_PROBLEM_TITLE_BAD_REQUEST = "problem.title.bad_request";
    private static final String KEY_PROBLEM_DETAIL_BAD_REQUEST = "problem.detail.bad_request";
    private static final String KEY_PROBLEM_DETAIL_PARAM_INVALID = "request.param.invalid";

    private static final String KEY_PROBLEM_TITLE_VALIDATION_FAILED = "problem.title.validation_failed";

    private static final String KEY_PROBLEM_TITLE_METHOD_NOT_ALLOWED = "problem.title.method_not_allowed";

    private static final String KEY_ENDPOINT_NOT_FOUND = "request.endpoint.not_found";
    private static final String KEY_METHOD_NOT_SUPPORTED = "request.method.not_supported";
    private static final String KEY_PARAM_REQUIRED_MISSING = "request.param.required_missing";
    private static final String KEY_HEADER_REQUIRED_MISSING = "request.header.missing";
    private static final String KEY_PARAM_TYPE_MISMATCH = "request.param.type_mismatch";

    private static final String KEY_REQUEST_BODY_INVALID = "request.body.invalid";
    private static final String KEY_REQUEST_BODY_FIELD_UNRECOGNIZED = "request.body.field.unrecognized";
    private static final String KEY_REQUEST_BODY_INVALID_FORMAT = "request.body.invalid_format";

    private static final String ERROR_CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    private static final String FALLBACK_INVALID = "invalid";
    private static final String FALLBACK_UNKNOWN = "unknown";

    private final LocalizedMessageResolver messageResolver;

    public ApiRequestExceptionHandler(LocalizedMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        List<ErrorItem> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(this::toErrorItem)
                        .toList();

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        VALIDATION_FAILED,
                        messageResolver.getMessage(KEY_PROBLEM_TITLE_VALIDATION_FAILED),
                        errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<LicenseAgentErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        List<ErrorItem> errors =
                ex.getConstraintViolations().stream()
                        .map(this::toErrorItem)
                        .toList();

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        VALIDATION_FAILED,
                        messageResolver.getMessage(KEY_PROBLEM_TITLE_VALIDATION_FAILED),
                        errors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<LicenseAgentErrorResponse> handleBindException(BindException ex) {

        List<ErrorItem> errors = ex.getFieldErrors().stream().map(this::toErrorItem).toList();

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        VALIDATION_FAILED,
                        messageResolver.getMessage(KEY_PROBLEM_TITLE_VALIDATION_FAILED),
                        errors));
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            return handleInvalidFormat(invalidFormatException);
        }

        if (cause instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
            return handleUnrecognized(unrecognizedPropertyException);
        }

        log.warn("Bad request (not readable): {}", ex.getMessage());

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        BAD_REQUEST,
                        messageResolver.getMessage(KEY_REQUEST_BODY_INVALID)));
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        log.warn("Endpoint not found: {}", ex.getResourcePath());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LicenseAgentErrorResponse.of(
                        NOT_FOUND,
                        messageResolver.getMessage(KEY_ENDPOINT_NOT_FOUND)));
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                LicenseAgentErrorResponse.of(
                        ERROR_CODE_METHOD_NOT_ALLOWED,
                        messageResolver.getMessage(KEY_METHOD_NOT_SUPPORTED, ex.getMethod())));
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String param = ex.getParameterName();

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        BAD_REQUEST,
                        messageResolver.getMessage(KEY_PARAM_REQUIRED_MISSING, param),
                        List.of(new ErrorItem(BAD_REQUEST, param, param, null, null))));
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleServletRequestBindingException(
            @NonNull ServletRequestBindingException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        if (ex instanceof MissingRequestHeaderException missingHeaderEx) {
            String header = missingHeaderEx.getHeaderName();

            return ResponseEntity.badRequest().body(
                    LicenseAgentErrorResponse.of(
                            BAD_REQUEST,
                            messageResolver.getMessage(KEY_HEADER_REQUIRED_MISSING, header)));
        }

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        BAD_REQUEST,
                        messageResolver.getMessage(KEY_PROBLEM_DETAIL_PARAM_INVALID)));
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String expected =
                Optional.ofNullable(ex.getRequiredType())
                        .map(Class::getSimpleName)
                        .orElse(FALLBACK_UNKNOWN);

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        BAD_REQUEST,
                        messageResolver.getMessage(KEY_PARAM_TYPE_MISMATCH, expected)));
    }

    private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException ex) {

        List<ErrorItem> errors =
                ex.getPath().stream()
                        .map(ref ->
                                new ErrorItem(
                                        BAD_REQUEST,
                                        messageResolver.getMessage(
                                                KEY_REQUEST_BODY_INVALID_FORMAT,
                                                ex.getTargetType().getSimpleName(),
                                                String.valueOf(ex.getValue())),
                                        ref.getFieldName(),
                                        null,
                                        null))
                        .toList();

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(BAD_REQUEST, messageResolver.getMessage(KEY_REQUEST_BODY_INVALID), errors));
    }

    private ResponseEntity<Object> handleUnrecognized(UnrecognizedPropertyException ex) {

        String field = ex.getPropertyName();

        return ResponseEntity.badRequest().body(
                LicenseAgentErrorResponse.of(
                        BAD_REQUEST,
                        messageResolver.getMessage(KEY_REQUEST_BODY_FIELD_UNRECOGNIZED, field)));
    }

    private ErrorItem toErrorItem(FieldError fe) {
        return new ErrorItem(VALIDATION_FAILED, resolve(fe.getDefaultMessage()), fe.getField(), null, null);
    }

    private ErrorItem toErrorItem(ConstraintViolation<?> v) {
        return new ErrorItem(VALIDATION_FAILED, resolve(v.getMessage()), v.getPropertyPath().toString(), null, null);
    }

    private String resolve(String key) {
        if (key == null) return FALLBACK_INVALID;
        String val = messageResolver.getMessage(key);
        return (val == null || val.isBlank()) ? key : val;
    }
}