package io.github.bsayli.licensing.service.exception.version;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseServiceVersionNotSupportedException extends ServiceException {

  public LicenseServiceVersionNotSupportedException(Object... args) {
    super(ServiceErrorCode.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, args);
  }

  public LicenseServiceVersionNotSupportedException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, cause, args);
  }
}
