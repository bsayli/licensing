package io.github.bsayli.licensing.service.exception.serviceid;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseInvalidServiceIdException extends ServiceException {

  public LicenseInvalidServiceIdException(Object... args) {
    super(ServiceErrorCode.LICENSE_INVALID_SERVICE_ID, args);
  }

  public LicenseInvalidServiceIdException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_INVALID_SERVICE_ID, cause, args);
  }
}
