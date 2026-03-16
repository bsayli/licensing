package io.github.bsayli.licensing.agent.api.exception;

import io.github.bsayli.apicontract.error.ErrorItem;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

import static io.github.bsayli.licensing.agent.api.exception.ProblemSupport.*;
import static io.github.bsayli.licensing.agent.common.api.ApiConstants.ErrorCode.VALIDATION_FAILED;

@RestControllerAdvice
@Order(1)
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String KEY_PROBLEM_TITLE_VALIDATION_FAILED = "problem.title.validation_failed";
    private static final String KEY_PROBLEM_DETAIL_VALIDATION_FAILED = "problem.detail.validation_failed";
    private static final String FALLBACK_INVALID = "invalid";

    private final LocalizedMessageResolver messageResolver;

    public ValidationExceptionHandler(LocalizedMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        HttpServletRequest req = ((ServletWebRequest) request).getRequest();

        List<ErrorItem> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(this::toErrorItem)
                        .toList();

        ProblemDetail pd = buildValidationProblem(req);
        attachErrors(pd, VALIDATION_FAILED, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErrorItem> errors =
                ex.getConstraintViolations().stream()
                        .map(this::toErrorItem)
                        .toList();

        ProblemDetail pd = buildValidationProblem(req);
        attachErrors(pd, VALIDATION_FAILED, errors);
        return pd;
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBindException(BindException ex, HttpServletRequest req) {
        List<ErrorItem> errors =
                ex.getFieldErrors().stream()
                        .map(this::toErrorItem)
                        .toList();

        ProblemDetail pd = buildValidationProblem(req);
        attachErrors(pd, VALIDATION_FAILED, errors);
        return pd;
    }

    private ProblemDetail buildValidationProblem(HttpServletRequest req) {
        return baseProblem(
                type(TYPE_VALIDATION_FAILED),
                HttpStatus.BAD_REQUEST,
                messageResolver.getMessage(KEY_PROBLEM_TITLE_VALIDATION_FAILED),
                messageResolver.getMessage(KEY_PROBLEM_DETAIL_VALIDATION_FAILED),
                req);
    }

    private ErrorItem toErrorItem(FieldError fe) {
        String field = fe.getField();
        String message = resolveMessageOrKey(fe.getDefaultMessage(), FALLBACK_INVALID);
        return error(VALIDATION_FAILED, message, field, null, null);
    }

    private ErrorItem toErrorItem(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();

        String resolvedTemplate = resolveMessageOrKey(v.getMessageTemplate(), null);
        String message =
                looksLikeMessageKeyTemplate(v.getMessageTemplate())
                        ? resolvedTemplate
                        : resolveMessageOrKey(v.getMessage(), FALLBACK_INVALID);

        return error(VALIDATION_FAILED, message, field, null, null);
    }

    private String resolveMessageOrKey(String keyOrText, String fallback) {
        if (keyOrText == null) return fallback;

        String s = keyOrText.trim();
        if (s.isEmpty()) return fallback;

        if (looksLikeMessageKeyTemplate(s)) {
            String key = extractKey(s);
            return messageResolver.getMessage(key);
        }

        return s;
    }

    private boolean looksLikeMessageKeyTemplate(String s) {
        return s != null && s.length() > 2 && s.startsWith("{") && s.endsWith("}");
    }

    private String extractKey(String template) {
        return template.substring(1, template.length() - 1).trim();
    }
}