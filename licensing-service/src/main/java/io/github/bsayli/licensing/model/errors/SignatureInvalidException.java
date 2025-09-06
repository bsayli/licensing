package io.github.bsayli.licensing.model.errors;

import io.github.bsayli.licensing.model.LicenseServiceStatus;

public class SignatureInvalidException extends LicenseServiceExceptionImpl {

  private static final long serialVersionUID = -5548456054825956055L;

  public SignatureInvalidException(String message) {
    super(message);
  }

  public SignatureInvalidException(String message, Throwable e) {
    super(message, e);
  }

  public LicenseServiceStatus getStatus() {
    return LicenseServiceStatus.SIGNATURE_INVALID;
  }
}
