package com.c9.licensing.model.errors;

public abstract class LicenseServiceExceptionImpl extends RuntimeException
    implements LicenseServiceException {

  private static final long serialVersionUID = 4327755923293926801L;

  protected LicenseServiceExceptionImpl(String message) {
    super(message);
  }

  protected LicenseServiceExceptionImpl(String message, Throwable cause) {
    super(message, cause);
  }
}
