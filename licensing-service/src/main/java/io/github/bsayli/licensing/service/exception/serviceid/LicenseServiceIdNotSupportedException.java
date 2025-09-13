package io.github.bsayli.licensing.service.exception.serviceid;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class LicenseServiceIdNotSupportedException extends ServiceException {

  public LicenseServiceIdNotSupportedException(String serviceId) {
    super(ServiceErrorCode.LICENSE_SERVICE_ID_NOT_SUPPORTED, serviceId);
  }

  public LicenseServiceIdNotSupportedException(Throwable cause, String serviceId) {
    super(ServiceErrorCode.LICENSE_SERVICE_ID_NOT_SUPPORTED, cause, serviceId);
  }
}
