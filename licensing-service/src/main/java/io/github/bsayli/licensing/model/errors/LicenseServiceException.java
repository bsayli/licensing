package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public interface LicenseServiceException {
  LicenseServiceStatus getStatus();
}
