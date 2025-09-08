package io.github.bsayli.licensing.service.exception.internal;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class InternalServerErrorException extends ServiceException {

  public InternalServerErrorException(Object... args) {
    super(ServiceErrorCode.INTERNAL_SERVER_ERROR, args);
  }

  public InternalServerErrorException(Throwable cause, Object... args) {
    super(ServiceErrorCode.INTERNAL_SERVER_ERROR, cause, args);
  }
}
