package io.github.bsayli.licensing.model.errors;

public class LicenseServiceVersionNotSupportedException extends LicenseServiceExceptionImpl {

  public LicenseServiceVersionNotSupportedException(Object... args) {
    super(LicenseServiceStatus.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, args);
  }

  public LicenseServiceVersionNotSupportedException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, cause, args);
  }
}
