package io.github.bsayli.licensing.model.errors;

public class LicenseUsageLimitExceededException extends LicenseServiceExceptionImpl {

  public LicenseUsageLimitExceededException(Object... args) {
    super(LicenseServiceStatus.LICENSE_USAGE_LIMIT_EXCEEDED, args);
  }

  public LicenseUsageLimitExceededException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_USAGE_LIMIT_EXCEEDED, cause, args);
  }
}
