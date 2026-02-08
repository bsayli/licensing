package io.github.bsayli.licensing.api.exception;

import io.github.bsayli.licensing.common.exception.ServiceException;
import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.repository.exception.RepositoryException;
import io.github.bsayli.licensing.service.user.exception.UserOperationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

import static io.github.bsayli.licensing.api.exception.ProblemSupport.*;
import static io.github.bsayli.licensing.common.api.ApiConstants.ErrorCode.INTERNAL_ERROR;
import static io.github.bsayli.licensing.common.api.ApiConstants.ErrorCode.NOT_FOUND;

@RestControllerAdvice(basePackages = "io.github.bsayli.licensing.api.controller")
@Order(4)
public class ApplicationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    private static final String KEY_PROBLEM_TITLE_NOT_FOUND = "problem.title.not_found";
    private static final String KEY_PROBLEM_DETAIL_NOT_FOUND = "problem.detail.not_found";

    private static final String KEY_PROBLEM_TITLE_INTERNAL_ERROR = "problem.title.internal_error";
    private static final String KEY_PROBLEM_DETAIL_INTERNAL_ERROR = "problem.detail.internal_error";

    private static final String KEY_PROBLEM_TITLE_SERVICE_ERROR = "problem.title.service_error";
    private static final String KEY_PROBLEM_DETAIL_SERVICE_ERROR = "problem.detail.service_error";

    private static final String KEY_PROBLEM_TITLE_CONFLICT = "problem.title.conflict";
    private static final String KEY_PROBLEM_DETAIL_CONFLICT = "problem.detail.conflict";

    private static final String KEY_PROBLEM_TITLE_TOO_MANY_REQUESTS = "problem.title.too_many_requests";
    private static final String KEY_PROBLEM_DETAIL_TOO_MANY_REQUESTS = "problem.detail.too_many_requests";

    private static final String KEY_LICENSE_VALIDATION_FAILED = "license.validation.failed";
    private static final String KEY_SERVER_INTERNAL_ERROR = "server.internal.error";
    private static final String KEY_USER_OPERATION_FAILED = "user.operation.failed";

    private static final String LOG_SERVICE_EXCEPTION =
            "Service exception handled. statusCode={}, code={}, messageKey={} ";
    private static final String LOG_REPOSITORY_EXCEPTION =
            "Repository exception handled. statusCode={}, errorCode={}, messageKey={} ";
    private static final String LOG_USER_OPERATION_EXCEPTION =
            "User operation exception handled. statusCode={}, errorCode={}, messageKey={} ";

    private final LocalizedMessageResolver messageResolver;

    private final Map<HttpStatus, ProblemDescriptor> repositoryProblems;
    private final Map<HttpStatus, ProblemDescriptor> userOperationProblems;

    public ApplicationExceptionHandler(LocalizedMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
        this.repositoryProblems = buildRepositoryProblems(messageResolver);
        this.userOperationProblems = buildUserOperationProblems(messageResolver);
    }

    private static Map<HttpStatus, ProblemDescriptor> buildRepositoryProblems(LocalizedMessageResolver mr) {
        Map<HttpStatus, ProblemDescriptor> m = new EnumMap<>(HttpStatus.class);

        m.put(
                HttpStatus.NOT_FOUND,
                new ProblemDescriptor(
                        TYPE_NOT_FOUND,
                        mr.getMessage(KEY_PROBLEM_TITLE_NOT_FOUND),
                        mr.getMessage(KEY_PROBLEM_DETAIL_NOT_FOUND)));

        m.put(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ProblemDescriptor(
                        TYPE_INTERNAL_ERROR,
                        mr.getMessage(KEY_PROBLEM_TITLE_INTERNAL_ERROR),
                        mr.getMessage(KEY_PROBLEM_DETAIL_INTERNAL_ERROR)));

        return Map.copyOf(m);
    }

    private static Map<HttpStatus, ProblemDescriptor> buildUserOperationProblems(LocalizedMessageResolver mr) {
        Map<HttpStatus, ProblemDescriptor> m = new EnumMap<>(HttpStatus.class);

        m.put(
                HttpStatus.CONFLICT,
                new ProblemDescriptor(
                        TYPE_CONFLICT,
                        mr.getMessage(KEY_PROBLEM_TITLE_CONFLICT),
                        mr.getMessage(KEY_PROBLEM_DETAIL_CONFLICT)));

        m.put(
                HttpStatus.TOO_MANY_REQUESTS,
                new ProblemDescriptor(
                        TYPE_TOO_MANY_REQUESTS,
                        mr.getMessage(KEY_PROBLEM_TITLE_TOO_MANY_REQUESTS),
                        mr.getMessage(KEY_PROBLEM_DETAIL_TOO_MANY_REQUESTS)));

        m.put(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ProblemDescriptor(
                        TYPE_INTERNAL_ERROR,
                        mr.getMessage(KEY_PROBLEM_TITLE_INTERNAL_ERROR),
                        mr.getMessage(KEY_PROBLEM_DETAIL_INTERNAL_ERROR)));

        return Map.copyOf(m);
    }

    @ExceptionHandler(ServiceException.class)
    public ProblemDetail handleServiceException(ServiceException ex, HttpServletRequest req) {
        HttpStatus http = ex.getHttpStatus();

        var logger = http.is5xxServerError() ? log.atError() : log.atWarn();
        logger
                .setCause(ex)
                .addArgument(http.value())
                .addArgument(ex.getCode())
                .addArgument(ex.getMessageKey())
                .log(LOG_SERVICE_EXCEPTION);

        String title = messageResolver.getMessage(KEY_LICENSE_VALIDATION_FAILED);
        String detail = messageResolver.getMessage(ex.getMessageKey(), ex.getArgs());

        ProblemDescriptor desc = problemServiceError();

        ProblemDetail pd = baseProblem(type(desc.typeSlug()), http, title, detail, req);

        attachErrors(
                pd,
                ex.getCode().name(),
                List.of(error(ex.getCode().name(), detail, null, null, null)));

        return pd;
    }

    @ExceptionHandler(RepositoryException.class)
    public ProblemDetail handleRepositoryException(RepositoryException ex, HttpServletRequest req) {
        HttpStatus http =
                switch (ex.getErrorCode()) {
                    case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case USER_ATTRIBUTE_MISSING, USER_ATTRIBUTE_INVALID_FORMAT -> HttpStatus.INTERNAL_SERVER_ERROR;
                };

        var logger = http.is5xxServerError() ? log.atError() : log.atWarn();
        logger
                .setCause(ex)
                .addArgument(http.value())
                .addArgument(ex.getErrorCode())
                .addArgument(ex.getMessageKey())
                .log(LOG_REPOSITORY_EXCEPTION);

        ProblemDescriptor desc = repositoryProblems.getOrDefault(http, problemInternalError());

        String detail = messageResolver.getMessage(ex.getMessageKey(), ex.getMessageArgs());
        ProblemDetail pd = baseProblem(type(desc.typeSlug()), http, desc.title(), detail, req);

        attachErrors(
                pd,
                ex.getErrorCode().name(),
                List.of(error(ex.getErrorCode().name(), detail, null, null, null)));

        return pd;
    }

    @ExceptionHandler(UserOperationException.class)
    public ProblemDetail handleUserOperationException(UserOperationException ex, HttpServletRequest req) {
        HttpStatus http =
                switch (ex.getErrorCode()) {
                    case ALREADY_PROCESSING -> HttpStatus.CONFLICT;
                    case MAX_RETRY_ATTEMPTS_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
                };

        var logger = http.is5xxServerError() ? log.atError() : log.atWarn();
        logger
                .setCause(ex)
                .addArgument(http.value())
                .addArgument(ex.getErrorCode())
                .addArgument(ex.getMessageKey())
                .log(LOG_USER_OPERATION_EXCEPTION);

        ProblemDescriptor desc = userOperationProblems.getOrDefault(http, problemInternalError());

        String title = messageResolver.getMessage(KEY_USER_OPERATION_FAILED);
        String detail = messageResolver.getMessage(ex.getMessageKey(), ex.getMessageArgs());

        ProblemDetail pd = baseProblem(type(desc.typeSlug()), http, title, detail, req);

        attachErrors(
                pd,
                ex.getErrorCode().name(),
                List.of(error(ex.getErrorCode().name(), detail, null, null, null)));

        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);

        ProblemDescriptor desc = problemInternalError();

        String detail = messageResolver.getMessage(KEY_SERVER_INTERNAL_ERROR);
        ProblemDetail pd =
                baseProblem(
                        type(desc.typeSlug()),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        desc.title(),
                        detail,
                        req);

        attachErrors(pd, INTERNAL_ERROR, List.of(error(INTERNAL_ERROR, detail, null, null, null)));
        return pd;
    }

    private ProblemDescriptor problemInternalError() {
        return new ProblemDescriptor(
                TYPE_INTERNAL_ERROR,
                messageResolver.getMessage(KEY_PROBLEM_TITLE_INTERNAL_ERROR),
                messageResolver.getMessage(KEY_PROBLEM_DETAIL_INTERNAL_ERROR));
    }

    private ProblemDescriptor problemServiceError() {
        return new ProblemDescriptor(
                TYPE_SERVICE_ERROR,
                messageResolver.getMessage(KEY_PROBLEM_TITLE_SERVICE_ERROR),
                messageResolver.getMessage(KEY_PROBLEM_DETAIL_SERVICE_ERROR));
    }
}