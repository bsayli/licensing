package io.github.bsayli.licensing.model.errors;

public class InvalidRequestException extends LicenseServiceExceptionImpl {

  public InvalidRequestException(Object... args) {
    super(LicenseServiceStatus.INVALID_REQUEST, args);
  }

  public InvalidRequestException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.INVALID_REQUEST, cause, args);
  }
}
