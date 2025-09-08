package io.github.bsayli.licensing.service.exception.request;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class InvalidParameterException extends ServiceException {

  public InvalidParameterException(Object... args) {
    super(ServiceErrorCode.INVALID_PARAMETER, args);
  }

  public InvalidParameterException(Throwable cause, Object... args) {
    super(ServiceErrorCode.INVALID_PARAMETER, cause, args);
  }
}
