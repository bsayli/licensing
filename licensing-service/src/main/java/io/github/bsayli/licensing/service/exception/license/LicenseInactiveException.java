package io.github.bsayli.licensing.service.exception.license;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseInactiveException extends ServiceException {

  public LicenseInactiveException(Object... args) {
    super(ServiceErrorCode.LICENSE_INACTIVE, args);
  }

  public LicenseInactiveException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_INACTIVE, cause, args);
  }
}
