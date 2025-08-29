package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public interface LicenseServiceException {
  LicenseServiceStatus getStatus();
}
