package io.github.bsayli.licensing.model;

import io.github.bsayli.licensing.model.errors.LicenseServiceStatus;

public record LicenseValidationResult(
    String userId,
    String appInstanceId,
    boolean valid,
    String licenseTier,
    String licenseStatus,
    LicenseServiceStatus serviceStatus,
    String message) {

  public static class Builder {
    private String userId;
    private String appInstanceId;
    private boolean valid;
    private String licenseTier;
    private String licenseStatus;
    private LicenseServiceStatus serviceStatus;
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

    public Builder licenseStatus(String licenseStatus) {
      this.licenseStatus = licenseStatus;
      return this;
    }

    public Builder serviceStatus(LicenseServiceStatus serviceStatus) {
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
