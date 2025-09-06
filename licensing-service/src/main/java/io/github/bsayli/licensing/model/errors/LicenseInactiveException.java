package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class LicenseInactiveException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -7483968084676456879L;

  public LicenseInactiveException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.LICENSE_INACTIVE;
  }
}
