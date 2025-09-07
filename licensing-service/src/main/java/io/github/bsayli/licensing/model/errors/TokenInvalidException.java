package io.github.bsayli.licensing.model.errors;

public class TokenInvalidException extends LicenseServiceExceptionImpl {

  public TokenInvalidException(Object... args) {
    super(LicenseServiceStatus.TOKEN_INVALID, args);
  }

  public TokenInvalidException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.TOKEN_INVALID, cause, args);
  }
}
