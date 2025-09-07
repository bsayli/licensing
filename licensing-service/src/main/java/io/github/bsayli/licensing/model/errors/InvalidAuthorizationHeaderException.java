package io.github.bsayli.licensing.model.errors;

public class InvalidAuthorizationHeaderException extends LicenseServiceExceptionImpl {

  public InvalidAuthorizationHeaderException(Object... args) {
    super(LicenseServiceStatus.INVALID_PARAMETER, args);
  }

  public InvalidAuthorizationHeaderException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.INVALID_PARAMETER, cause, args);
  }

  @Override
  public String getMessageKey() {
    return "request.header.authorization.invalid";
  }
}
