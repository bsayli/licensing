package io.github.bsayli.licensing.service.exception.serviceid;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseInvalidChecksumException extends ServiceException {

  public LicenseInvalidChecksumException(Object... args) {
    super(ServiceErrorCode.LICENSE_INVALID_CHECKSUM, args);
  }

  public LicenseInvalidChecksumException(Throwable cause, Object... args) {
    super(ServiceErrorCode.LICENSE_INVALID_CHECKSUM, cause, args);
  }
}
