package io.github.bsayli.licensing.service.exception.license;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseInvalidException extends ServiceException {

  public LicenseInvalidException(Object... args) {
    super(ServiceErrorCode.LICENSE_INVALID, args);
  }

  public LicenseInvalidException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_INVALID, cause, args);
  }
}
