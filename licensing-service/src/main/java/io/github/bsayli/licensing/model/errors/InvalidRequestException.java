package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class InvalidRequestException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -5901352589607005346L;

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable e) {
    super(message, e);
  }

  @Override
  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.INVALID_REQUEST;
  }
}
