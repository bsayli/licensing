package io.github.bsayli.licensing.model.errors;

public class LicenseExpiredException extends LicenseServiceExceptionImpl {

  public LicenseExpiredException(Object... args) {
    super(LicenseServiceStatus.LICENSE_EXPIRED, args);
  }

  public LicenseExpiredException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_EXPIRED, cause, args);
  }
}
