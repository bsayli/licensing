package io.github.bsayli.licensing.model.errors;

public class SignatureInvalidException extends LicenseServiceExceptionImpl {

  public SignatureInvalidException(Object... args) {
    super(LicenseServiceStatus.SIGNATURE_INVALID, args);
  }

  public SignatureInvalidException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.SIGNATURE_INVALID, cause, args);
  }
}
