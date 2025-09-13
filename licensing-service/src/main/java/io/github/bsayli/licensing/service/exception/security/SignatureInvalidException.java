package io.github.bsayli.licensing.service.exception.security;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.common.exception.ServiceException;

public class SignatureInvalidException extends ServiceException {

  public SignatureInvalidException(Object... args) {
    super(ServiceErrorCode.SIGNATURE_INVALID, args);
  }

  public SignatureInvalidException(Throwable cause, Object... args) {
    super(ServiceErrorCode.SIGNATURE_INVALID, cause, args);
  }
}
