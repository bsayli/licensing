package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class LicenseServiceIdNotSupportedException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -270413362582712000L;

  public LicenseServiceIdNotSupportedException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.LICENSE_SERVICE_ID_NOT_SUPPORTED;
  }
}
