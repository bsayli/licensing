package io.github.bsayli.licensing.agent.api.exception;

import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static io.github.bsayli.licensing.agent.api.exception.ProblemSupport.*;
import static io.github.bsayli.licensing.agent.common.api.ApiConstants.ErrorCode.INTERNAL_ERROR;

@RestControllerAdvice(basePackages = "io.github.bsayli.licensing.agent.api.controller")
@Order(4)
public class ApplicationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    private static final String KEY_PROBLEM_TITLE_INTERNAL_ERROR = "problem.title.internal_error";
    private static final String KEY_PROBLEM_DETAIL_INTERNAL_ERROR = "problem.detail.internal_error";

    private static final String KEY_PROBLEM_TITLE_SERVICE_ERROR = "problem.title.service_error";
    private static final String KEY_PROBLEM_DETAIL_SERVICE_ERROR = "problem.detail.service_error";

    private static final String KEY_SERVER_INTERNAL_ERROR = "server.internal.error";
    private static final String KEY_LICENSE_VALIDATION_FAILED = "license.validation.failed";

    private final LocalizedMessageResolver messageResolver;

    public ApplicationExceptionHandler(LocalizedMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @ExceptionHandler(LicensingAgentRemoteServiceException.class)
    public ProblemDetail handleRemoteService(
            LicensingAgentRemoteServiceException ex, HttpServletRequest req) {

        HttpStatus http =
                ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_GATEWAY;

        log.atWarn()
                .setCause(ex)
                .log("Remote service error handled. status={}", http.value());

        ProblemDescriptor desc = problemServiceError();

        String title = messageResolver.getMessage(KEY_LICENSE_VALIDATION_FAILED);
        String detail =
                ex.getTopMessage() != null && !ex.getTopMessage().isBlank()
                        ? ex.getTopMessage()
                        : messageResolver.getMessage(KEY_LICENSE_VALIDATION_FAILED);

        ProblemDetail pd = baseProblem(type(desc.typeSlug()), http, title, detail, req);

        attachErrors(
                pd,
                ex.getErrorCode() != null ? ex.getErrorCode() : INTERNAL_ERROR,
                ex.getDetails() == null || ex.getDetails().isEmpty()
                        ? List.of(error(INTERNAL_ERROR, detail, null, null, null))
                        : ex.getDetails().stream()
                        .map(d -> error(ex.getErrorCode(), d, null, null, null))
                        .toList());

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

        attachErrors(
                pd,
                INTERNAL_ERROR,
                List.of(error(INTERNAL_ERROR, detail, null, null, null)));

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