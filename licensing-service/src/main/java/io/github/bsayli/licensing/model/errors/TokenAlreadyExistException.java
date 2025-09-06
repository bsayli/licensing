package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class TokenAlreadyExistException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = 9039245175884123100L;

  public TokenAlreadyExistException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.TOKEN_ALREADY_EXIST;
  }
}
