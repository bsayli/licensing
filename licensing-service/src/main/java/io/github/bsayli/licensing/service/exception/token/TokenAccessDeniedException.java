package io.github.bsayli.licensing.service.exception.token;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class TokenAccessDeniedException extends ServiceException {

  public TokenAccessDeniedException(Object... args) {
    super(ServiceErrorCode.TOKEN_INVALID_ACCESS, args);
  }

  public TokenAccessDeniedException(Throwable cause, Object... args) {
    super(ServiceErrorCode.TOKEN_INVALID_ACCESS, cause, args);
  }
}
