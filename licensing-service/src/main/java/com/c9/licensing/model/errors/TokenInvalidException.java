package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class TokenInvalidException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = 4398609211361201185L;

  public TokenInvalidException(String message) {
    super(message);
  }

  public TokenInvalidException(String message, Throwable e) {
    super(message, e);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.TOKEN_INVALID;
  }
}
