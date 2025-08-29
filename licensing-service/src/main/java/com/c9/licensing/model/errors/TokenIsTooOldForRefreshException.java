package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class TokenIsTooOldForRefreshException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = 3955743789541161523L;

  public TokenIsTooOldForRefreshException(String message) {
    super(message);
  }

  public TokenIsTooOldForRefreshException(String message, Throwable e) {
    super(message, e);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.TOKEN_IS_TOO_OLD_FOR_REFRESH;
  }
}
