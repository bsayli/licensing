package io.github.bsayli.licensing.service.exception.license;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseNotFoundException extends ServiceException {
  public LicenseNotFoundException(Object... args) {
    super(ServiceErrorCode.LICENSE_NOT_FOUND, args);
  }

  public LicenseNotFoundException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_NOT_FOUND, cause, args);
  }
}
