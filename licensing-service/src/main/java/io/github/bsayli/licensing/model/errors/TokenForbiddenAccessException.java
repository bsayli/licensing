package io.github.bsayli.licensing.model.errors;

public class TokenForbiddenAccessException extends LicenseServiceExceptionImpl {

  public TokenForbiddenAccessException(Object... args) {
    super(LicenseServiceStatus.TOKEN_INVALID_ACCESS, args);
  }

  public TokenForbiddenAccessException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.TOKEN_INVALID_ACCESS, cause, args);
  }
}
