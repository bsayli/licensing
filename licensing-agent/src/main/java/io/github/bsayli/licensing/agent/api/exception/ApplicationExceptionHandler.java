package io.github.bsayli.licensing.agent.api.exception;

import io.github.bsayli.licensing.agent.api.dto.LicenseAgentErrorResponse;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(2)
public class ApplicationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    private static final String KEY_SERVER_INTERNAL_ERROR = "server.internal.error";

    private final LocalizedMessageResolver messageResolver;

    public ApplicationExceptionHandler(LocalizedMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

  @ExceptionHandler(LicensingAgentRemoteServiceException.class)
  public ResponseEntity<LicenseAgentErrorResponse> handleRemoteService(
      LicensingAgentRemoteServiceException ex) {

        HttpStatus http =
                ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_GATEWAY;

    log.warn("Remote service error handled. status={}", http.value(), ex);

    return ResponseEntity.status(http)
        .body(LicenseAgentErrorResponse.of(ex.getErrorCode(), ex.getTopMessage()));
    }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<LicenseAgentErrorResponse> handleGeneric(Exception ex) {

        log.error("Unhandled exception", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            LicenseAgentErrorResponse.of(
                "INTERNAL_ERROR", messageResolver.getMessage(KEY_SERVER_INTERNAL_ERROR)));
    }
}