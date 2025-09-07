package io.github.bsayli.licensing.model.errors;

public class TokenAlreadyExistException extends LicenseServiceExceptionImpl {

  public TokenAlreadyExistException(Object... args) {
    super(LicenseServiceStatus.TOKEN_ALREADY_EXIST, args);
  }

  public TokenAlreadyExistException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.TOKEN_ALREADY_EXIST, cause, args);
  }
}
