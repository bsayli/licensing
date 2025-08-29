package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseInvalidServiceIdException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = 7668179223994149753L;

  public LicenseInvalidServiceIdException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.LICENSE_INVALID_SERVICE_ID;
  }
}
