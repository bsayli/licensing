package io.github.bsayli.licensing.service.exception.internal;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseServiceInternalException extends ServiceException {

  public LicenseServiceInternalException(Object... args) {
    super(ServiceErrorCode.INTERNAL_SERVER_ERROR, args);
  }

  public LicenseServiceInternalException(Throwable cause, Object... args) {
    super(ServiceErrorCode.INTERNAL_SERVER_ERROR, cause, args);
  }
}
