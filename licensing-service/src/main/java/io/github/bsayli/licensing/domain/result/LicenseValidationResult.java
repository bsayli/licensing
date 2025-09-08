package io.github.bsayli.licensing.domain.result;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.LicenseStatus;

public record LicenseValidationResult(
    String userId,
    String appInstanceId,
    boolean valid,
    String licenseTier,
    LicenseStatus licenseStatus,
    ServiceErrorCode serviceStatus,
    String message) {

  public static class Builder {
    private String userId;
    private String appInstanceId;
    private boolean valid;
    private String licenseTier;
    private LicenseStatus licenseStatus;
    private ServiceErrorCode serviceStatus;
    private String message;

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder appInstanceId(String appInstanceId) {
      this.appInstanceId = appInstanceId;
      return this;
    }

    public Builder valid(boolean valid) {
      this.valid = valid;
      return this;
    }

    public Builder licenseTier(String licenseTier) {
      this.licenseTier = licenseTier;
      return this;
    }

    public Builder licenseStatus(LicenseStatus licenseStatus) {
      this.licenseStatus = licenseStatus;
      return this;
    }

    public Builder serviceStatus(ServiceErrorCode serviceStatus) {
      this.serviceStatus = serviceStatus;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public LicenseValidationResult build() {
      return new LicenseValidationResult(
          userId, appInstanceId, valid, licenseTier, licenseStatus, serviceStatus, message);
    }
  }
}
