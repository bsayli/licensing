package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class LicenseServiceVersionNotSupportedException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -6851648046615340071L;

  public LicenseServiceVersionNotSupportedException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.LICENSE_SERVICE_VERSION_NOT_SUPPORTED;
  }
}
