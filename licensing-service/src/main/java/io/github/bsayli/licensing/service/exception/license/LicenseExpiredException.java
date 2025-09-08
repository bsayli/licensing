package io.github.bsayli.licensing.service.exception.license;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseExpiredException extends ServiceException {

  public LicenseExpiredException(Object... args) {
    super(ServiceErrorCode.LICENSE_EXPIRED, args);
  }

  public LicenseExpiredException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_EXPIRED, cause, args);
  }
}
