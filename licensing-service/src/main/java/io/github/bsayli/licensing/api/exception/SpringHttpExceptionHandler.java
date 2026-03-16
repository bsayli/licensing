package io.github.bsayli.licensing.api.exception;

import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Optional;

import static io.github.bsayli.licensing.api.exception.ProblemSupport.*;
import static io.github.bsayli.licensing.common.api.ApiConstants.ErrorCode.BAD_REQUEST;
import static io.github.bsayli.licensing.common.api.ApiConstants.ErrorCode.NOT_FOUND;

@RestControllerAdvice
@Order(3)
public class SpringHttpExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SpringHttpExceptionHandler.class);

    private static final String KEY_PROBLEM_TITLE_NOT_FOUND = "problem.title.not_found";
    private static final String KEY_PROBLEM_DETAIL_NOT_FOUND = "problem.detail.not_found";

    private static final String KEY_PROBLEM_TITLE_BAD_REQUEST = "problem.title.bad_request";
    private static final String KEY_PROBLEM_DETAIL_PARAM_INVALID = "request.param.invalid";

    private static final String KEY_PROBLEM_TITLE_METHOD_NOT_ALLOWED = "problem.title.method_not_allowed";
    private static final String KEY_PROBLEM_DETAIL_METHOD_NOT_ALLOWED = "problem.detail.method_not_allowed";

    private static final String KEY_ENDPOINT_NOT_FOUND = "request.endpoint.not_found";
    private static final String KEY_METHOD_NOT_SUPPORTED = "request.method.not_supported";
    private static final String KEY_PARAM_REQUIRED_MISSING = "request.param.required_missing";
    private static final String KEY_HEADER_REQUIRED_MISSING = "request.header.missing";
    private static final String KEY_PARAM_TYPE_MISMATCH = "request.param.type_mismatch";

    private static final String ERROR_CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    private static final String FALLBACK_UNKNOWN = "unknown";

    private final LocalizedMessageResolver messageResolver;

    public SpringHttpExceptionHandler(LocalizedMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        HttpServletRequest req = ((ServletWebRequest) request).getRequest();

        log.warn("Endpoint not found: {}", ex.getResourcePath());

        ProblemDetail pd =
                baseProblem(
                        type(TYPE_NOT_FOUND),
                        HttpStatus.NOT_FOUND,
                        messageResolver.getMessage(KEY_PROBLEM_TITLE_NOT_FOUND),
                        messageResolver.getMessage(KEY_PROBLEM_DETAIL_NOT_FOUND),
                        req);

        attachErrors(
                pd,
                NOT_FOUND,
                List.of(
                        error(
                                NOT_FOUND,
                                messageResolver.getMessage(KEY_ENDPOINT_NOT_FOUND),
                                null,
                                null,
                                null)));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        HttpServletRequest req = ((ServletWebRequest) request).getRequest();
        String method = ex.getMethod();

        ProblemDetail pd =
                baseProblem(
                        type(TYPE_METHOD_NOT_ALLOWED),
                        HttpStatus.METHOD_NOT_ALLOWED,
                        messageResolver.getMessage(KEY_PROBLEM_TITLE_METHOD_NOT_ALLOWED),
                        messageResolver.getMessage(KEY_PROBLEM_DETAIL_METHOD_NOT_ALLOWED),
                        req);

        attachErrors(
                pd,
                ERROR_CODE_METHOD_NOT_ALLOWED,
                List.of(
                        error(
                                ERROR_CODE_METHOD_NOT_ALLOWED,
                                messageResolver.getMessage(KEY_METHOD_NOT_SUPPORTED, method),
                                null,
                                null,
                                null)));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(pd);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        HttpServletRequest req = ((ServletWebRequest) request).getRequest();
        String param = ex.getParameterName();

        ProblemDetail pd = buildBadRequestParamProblem(req);

        attachErrors(
                pd,
                BAD_REQUEST,
                List.of(
                        error(
                                BAD_REQUEST,
                                messageResolver.getMessage(KEY_PARAM_REQUIRED_MISSING, param),
                                param,
                                null,
                                null)));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        HttpServletRequest req = ((ServletWebRequest) request).getRequest();
        ProblemDetail pd = buildBadRequestParamProblem(req);

        if (ex instanceof MissingRequestHeaderException missingHeaderEx) {
            String header = missingHeaderEx.getHeaderName();

            attachErrors(
                    pd,
                    BAD_REQUEST,
                    List.of(
                            error(
                                    BAD_REQUEST,
                                    messageResolver.getMessage(KEY_HEADER_REQUIRED_MISSING, header),
                                    header,
                                    null,
                                    null)));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
        }

        attachErrors(
                pd,
                BAD_REQUEST,
                List.of(
                        error(
                                BAD_REQUEST,
                                messageResolver.getMessage(KEY_PROBLEM_DETAIL_PARAM_INVALID),
                                null,
                                null,
                                null)));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        HttpServletRequest req = ((ServletWebRequest) request).getRequest();

        String paramName =
                ex instanceof MethodArgumentTypeMismatchException matme
                        ? matme.getName()
                        : null;

        String expected =
                Optional.ofNullable(ex.getRequiredType())
                        .map(Class::getSimpleName)
                        .orElse(FALLBACK_UNKNOWN);

        ProblemDetail pd = buildBadRequestParamProblem(req);

        attachErrors(
                pd,
                BAD_REQUEST,
                List.of(
                        error(
                                BAD_REQUEST,
                                messageResolver.getMessage(KEY_PARAM_TYPE_MISMATCH, expected),
                                paramName,
                                null,
                                null)));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    private ProblemDetail buildBadRequestParamProblem(HttpServletRequest req) {
        return baseProblem(
                type(TYPE_BAD_REQUEST),
                HttpStatus.BAD_REQUEST,
                messageResolver.getMessage(KEY_PROBLEM_TITLE_BAD_REQUEST),
                messageResolver.getMessage(KEY_PROBLEM_DETAIL_PARAM_INVALID),
                req);
    }
}