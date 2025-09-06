package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class InvalidParameterException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = 324256173896067565L;

  public InvalidParameterException(String message) {
    super(message);
  }

  public InvalidParameterException(String message, Throwable e) {
    super(message, e);
  }

  @Override
  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.INVALID_PARAMETER;
  }
}
