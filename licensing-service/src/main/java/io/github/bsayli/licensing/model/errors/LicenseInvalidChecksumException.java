package io.github.bsayli.licensing.model.errors;

public class LicenseInvalidChecksumException extends LicenseServiceExceptionImpl {

  public LicenseInvalidChecksumException(Object... args) {
    super(LicenseServiceStatus.LICENSE_INVALID_CHECKSUM, args);
  }

  public LicenseInvalidChecksumException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.LICENSE_INVALID_CHECKSUM, cause, args);
  }
}
