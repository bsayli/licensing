package io.github.bsayli.licensing.service.exception.token;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class TokenIsTooOldForRefreshException extends ServiceException {

  public TokenIsTooOldForRefreshException(Object... args) {
    super(ServiceErrorCode.TOKEN_IS_TOO_OLD_FOR_REFRESH, args);
  }

  public TokenIsTooOldForRefreshException(Throwable cause, Object... args) {
    super(ServiceErrorCode.TOKEN_IS_TOO_OLD_FOR_REFRESH, cause, args);
  }
}
