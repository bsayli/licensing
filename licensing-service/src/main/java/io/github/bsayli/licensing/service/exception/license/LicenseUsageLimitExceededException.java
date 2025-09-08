package io.github.bsayli.licensing.service.exception.license;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseUsageLimitExceededException extends ServiceException {

  public LicenseUsageLimitExceededException(Object... args) {
    super(ServiceErrorCode.LICENSE_USAGE_LIMIT_EXCEEDED, args);
  }

  public LicenseUsageLimitExceededException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_USAGE_LIMIT_EXCEEDED, cause, args);
  }
}
