package io.github.bsayli.licensing.model.errors;

public class LicenseInvalidServiceIdException extends LicenseServiceExceptionImpl {

  public LicenseInvalidServiceIdException(Object... args) {
    super(LicenseServiceStatus.LICENSE_INVALID_SERVICE_ID, args);
  }

  public LicenseInvalidServiceIdException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_INVALID_SERVICE_ID, cause, args);
  }
}
