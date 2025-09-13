package io.github.bsayli.licensing.service.exception.token;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class TokenInvalidException extends ServiceException {

  public TokenInvalidException(Object... args) {
    super(ServiceErrorCode.TOKEN_INVALID, args);
  }

  public TokenInvalidException(Throwable cause, Object... args) {
    super(ServiceErrorCode.TOKEN_INVALID, cause, args);
  }
}
