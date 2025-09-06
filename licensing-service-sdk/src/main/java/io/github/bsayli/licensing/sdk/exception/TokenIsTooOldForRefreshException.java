package io.github.bsayli.licensing.sdk.exception;

import io.github.bsayli.licensing.sdk.model.server.LicenseServerServiceStatus;

public class TokenIsTooOldForRefreshException extends RuntimeException {

  private static final long serialVersionUID = 1709546690947598272L;

  public TokenIsTooOldForRefreshException(String message) {
    super(message);
  }

  public TokenIsTooOldForRefreshException(String message, Throwable e) {
    super(message, e);
  }

  public LicenseServerServiceStatus getStatus() {
    return LicenseServerServiceStatus.TOKEN_IS_TOO_OLD_FOR_REFRESH;
  }
}
