package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class LicenseExpiredException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -4216944794327386510L;

  public LicenseExpiredException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.LICENSE_EXPIRED;
  }
}
