package io.github.bsayli.licensing.common.exception;

import org.springframework.http.HttpStatus;

public enum ServiceErrorCode {
  LICENSE_NOT_FOUND("license.not.found", HttpStatus.NOT_FOUND),

  LICENSE_INVALID("license.invalid", HttpStatus.UNAUTHORIZED),
  LICENSE_EXPIRED("license.expired", HttpStatus.UNAUTHORIZED),
  LICENSE_INACTIVE("license.inactive", HttpStatus.UNAUTHORIZED),
  LICENSE_USAGE_LIMIT_EXCEEDED("license.usage.limit.exceeded", HttpStatus.UNAUTHORIZED),
  LICENSE_SERVICE_ID_NOT_SUPPORTED("license.service.id.not.supported", HttpStatus.UNAUTHORIZED),
  LICENSE_INVALID_SERVICE_ID("license.service.id.invalid", HttpStatus.UNAUTHORIZED),
  LICENSE_INVALID_CHECKSUM("license.checksum.invalid", HttpStatus.UNAUTHORIZED),
  LICENSE_SERVICE_VERSION_NOT_SUPPORTED(
      "license.service.version.not.supported", HttpStatus.UNAUTHORIZED),

  TOKEN_CREATED("license.token.created", HttpStatus.OK),
  TOKEN_EXPIRED("license.token.expired", HttpStatus.UNAUTHORIZED),
  TOKEN_INVALID("license.token.invalid", HttpStatus.UNAUTHORIZED),
  TOKEN_INVALID_ACCESS("license.token.invalid.access", HttpStatus.FORBIDDEN),
  TOKEN_REFRESHED("license.token.refreshed", HttpStatus.OK),
  TOKEN_ALREADY_EXIST("license.token.already.exists", HttpStatus.CONFLICT),
  TOKEN_ACTIVE("license.token.active", HttpStatus.OK),
  TOKEN_IS_TOO_OLD_FOR_REFRESH("license.token.too.old", HttpStatus.UNAUTHORIZED),

  SIGNATURE_INVALID("license.signature.invalid", HttpStatus.UNAUTHORIZED),

  INVALID_PARAMETER("request.invalid.parameter", HttpStatus.BAD_REQUEST),
  MISSING_PARAMETER("request.missing.parameter", HttpStatus.BAD_REQUEST),
  INVALID_REQUEST("request.invalid", HttpStatus.BAD_REQUEST),

  INTERNAL_SERVER_ERROR("server.internal.error", HttpStatus.INTERNAL_SERVER_ERROR),
  UNKNOWN_ERROR("server.unknown.error", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String messageKey;
  private final HttpStatus httpStatus;

  ServiceErrorCode(String messageKey, HttpStatus httpStatus) {
    this.messageKey = messageKey;
    this.httpStatus = httpStatus;
  }

  public String messageKey() {
    return messageKey;
  }

  public HttpStatus httpStatus() {
    return httpStatus;
  }
}
