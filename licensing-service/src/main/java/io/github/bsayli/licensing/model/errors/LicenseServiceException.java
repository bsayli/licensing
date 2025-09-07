package io.github.bsayli.licensing.model.errors;

public interface LicenseServiceException {
  LicenseServiceStatus getStatus();

  String getMessageKey();

  Object[] getMessageArgs();
}
