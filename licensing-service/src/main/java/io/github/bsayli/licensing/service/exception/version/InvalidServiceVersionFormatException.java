package io.github.bsayli.licensing.service.exception.version;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class InvalidServiceVersionFormatException extends ServiceException {

  public InvalidServiceVersionFormatException(String version) {
    super(ServiceErrorCode.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, version);
  }

  public InvalidServiceVersionFormatException(String version, Throwable cause) {
    super(ServiceErrorCode.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, cause, version);
  }
}
