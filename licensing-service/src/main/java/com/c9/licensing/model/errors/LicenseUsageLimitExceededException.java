package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseUsageLimitExceededException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -7483968084676456879L;

  public LicenseUsageLimitExceededException(String message) {
    super(message);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.LICENSE_USAGE_LIMIT_EXCEEDED;
  }
}
