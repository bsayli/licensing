package io.github.bsayli.licensing.model.errors;

public class LicenseServiceIdNotSupportedException extends LicenseServiceExceptionImpl {

  public LicenseServiceIdNotSupportedException(String serviceId) {
    super(LicenseServiceStatus.LICENSE_SERVICE_ID_NOT_SUPPORTED, serviceId);
  }

  public LicenseServiceIdNotSupportedException(Throwable cause, String serviceId) {
    super(LicenseServiceStatus.LICENSE_SERVICE_ID_NOT_SUPPORTED, cause, serviceId);
  }
}
