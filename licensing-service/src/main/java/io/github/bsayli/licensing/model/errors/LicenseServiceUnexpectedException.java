package io.github.bsayli.licensing.model.errors;

public class LicenseServiceUnexpectedException extends LicenseServiceExceptionImpl {

  public LicenseServiceUnexpectedException(Object... args) {
    super(LicenseServiceStatus.INTERNAL_SERVER_ERROR, args);
  }

  public LicenseServiceUnexpectedException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.INTERNAL_SERVER_ERROR, cause, args);
  }
}
