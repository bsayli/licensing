package io.github.bsayli.licensing.service.exception.token;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class TokenAlreadyExistsException extends ServiceException {

  public TokenAlreadyExistsException(Object... args) {
    super(ServiceErrorCode.TOKEN_ALREADY_EXIST, args);
  }

  public TokenAlreadyExistsException(Throwable cause, Object... args) {
    super(ServiceErrorCode.TOKEN_ALREADY_EXIST, cause, args);
  }
}
