package io.github.bsayli.licensing.model.errors;

public class LicenseInvalidException extends LicenseServiceExceptionImpl {

  public LicenseInvalidException(Object... args) {
    super(LicenseServiceStatus.LICENSE_INVALID, args);
  }

  public LicenseInvalidException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_INVALID, cause, args);
  }
}
