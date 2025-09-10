package io.github.bsayli.licensing.sdk.common.exception;

public enum ServiceErrorCode {
  LICENSE_NOT_FOUND("license.not.found"),
  LICENSE_INVALID("license.invalid"),
  LICENSE_EXPIRED("license.expired"),
  LICENSE_INACTIVE("license.inactive"),
  LICENSE_USAGE_LIMIT_EXCEEDED("license.usage.limit.exceeded"),
  LICENSE_SERVICE_ID_NOT_SUPPORTED("license.service.id.not.supported"),
  LICENSE_INVALID_SERVICE_ID("license.service.id.invalid"),
  LICENSE_INVALID_CHECKSUM("license.checksum.invalid"),
  LICENSE_SERVICE_VERSION_NOT_SUPPORTED("license.service.version.not.supported"),

  TOKEN_CREATED("license.token.created"),
  TOKEN_EXPIRED("license.token.expired"),
  TOKEN_INVALID("license.token.invalid"),
  TOKEN_INVALID_ACCESS("license.token.invalid.access"),
  TOKEN_REFRESHED("license.token.refreshed"),
  TOKEN_ALREADY_EXIST("license.token.already.exists"),
  TOKEN_ACTIVE("license.token.active"),
  TOKEN_IS_TOO_OLD_FOR_REFRESH("license.token.too.old"),

  SIGNATURE_INVALID("license.signature.invalid"),

  INVALID_PARAMETER("request.invalid.parameter"),
  MISSING_PARAMETER("request.missing.parameter"),
  INVALID_REQUEST("request.invalid"),

  INTERNAL_SERVER_ERROR("server.internal.error"),
  UNKNOWN_ERROR("server.unknown.error");

  private final String messageKey;

  ServiceErrorCode(String messageKey) {
    this.messageKey = messageKey;
  }

  public String messageKey() {
    return messageKey;
  }
}
