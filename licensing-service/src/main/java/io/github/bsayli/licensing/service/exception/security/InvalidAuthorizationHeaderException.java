package io.github.bsayli.licensing.service.exception.security;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class InvalidAuthorizationHeaderException extends ServiceException {

  public InvalidAuthorizationHeaderException(Object... args) {
    super(ServiceErrorCode.INVALID_PARAMETER, args);
  }

  public InvalidAuthorizationHeaderException(Throwable cause, Object... args) {
    super(ServiceErrorCode.INVALID_PARAMETER, cause, args);
  }

  @Override
  public String getMessageKey() {
    return "request.header.authorization.invalid";
  }
}
