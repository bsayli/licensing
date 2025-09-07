package io.github.bsayli.licensing.model.errors;

public class InvalidParameterException extends LicenseServiceExceptionImpl {

  public InvalidParameterException(Object... args) {
    super(LicenseServiceStatus.INVALID_PARAMETER, args);
  }

  public InvalidParameterException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.INVALID_PARAMETER, cause, args);
  }
}
