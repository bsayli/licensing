package io.github.bsayli.licensing.service.exception.token;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class TokenExpiredException extends ServiceException {

  private final String encUserId;

  public TokenExpiredException(String encUserId, Object... args) {
    super(ServiceErrorCode.TOKEN_EXPIRED, args);
    this.encUserId = encUserId;
  }

  public TokenExpiredException(String encUserId, Throwable cause, Object... args) {
    super(ServiceErrorCode.TOKEN_EXPIRED, cause, args);
    this.encUserId = encUserId;
  }

  public String getEncUserId() {
    return encUserId;
  }
}
