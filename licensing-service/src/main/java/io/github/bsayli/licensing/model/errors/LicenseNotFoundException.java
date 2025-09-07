package io.github.bsayli.licensing.model.errors;

public class LicenseNotFoundException extends LicenseServiceExceptionImpl {
  public LicenseNotFoundException(Object... args) {
    super(LicenseServiceStatus.LICENSE_NOT_FOUND, args);
  }

  public LicenseNotFoundException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_NOT_FOUND, cause, args);
  }
}
