package io.github.bsayli.licensing.model.errors;

public class InvalidServiceVersionFormatException extends LicenseServiceExceptionImpl {

  public InvalidServiceVersionFormatException(String version) {
    super(LicenseServiceStatus.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, version);
  }

  public InvalidServiceVersionFormatException(String version, Throwable cause) {
    super(LicenseServiceStatus.LICENSE_SERVICE_VERSION_NOT_SUPPORTED, cause, version);
  }
}
