package io.github.bsayli.licensing.service.exception.request;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class InvalidRequestException extends ServiceException {

  public InvalidRequestException(Object... args) {
    super(ServiceErrorCode.INVALID_REQUEST, args);
  }

  public InvalidRequestException(Throwable cause, Object... args) {
    super(ServiceErrorCode.INVALID_REQUEST, cause, args);
  }
}
