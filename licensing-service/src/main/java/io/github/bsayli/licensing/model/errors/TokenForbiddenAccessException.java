package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class TokenForbiddenAccessException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -2458534544543529495L;

  public TokenForbiddenAccessException(String message) {
    super(message);
  }

  public TokenForbiddenAccessException(String message, Throwable e) {
    super(message, e);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.TOKEN_INVALID_ACCESS;
  }
}
