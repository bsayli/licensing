package io.github.bsayli.licensing.model.errors;

public class LicenseInactiveException extends LicenseServiceExceptionImpl {

  public LicenseInactiveException(Object... args) {
    super(LicenseServiceStatus.LICENSE_INACTIVE, args);
  }

  public LicenseInactiveException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_INACTIVE, cause, args);
  }
}
