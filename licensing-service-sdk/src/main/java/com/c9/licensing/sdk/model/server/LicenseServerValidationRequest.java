package com.c9.licensing.sdk.model.server;

public record LicenseServerValidationRequest(
    String serviceId,
    String serviceVersion,
    String licenseKey,
    String licenseToken,
    String instanceId,
    String checksum,
    boolean forceTokenRefresh) {

  public static class Builder {
    private String licenseKey;
    private String serviceId;
    private String serviceVersion;
    private String licenseToken;
    private String instanceId;
    private String checksum;
    private boolean forceTokenRefresh;

    public Builder licenseKey(String licenseKey) {
      this.licenseKey = licenseKey;
      return this;
    }

    public Builder serviceId(String serviceId) {
      this.serviceId = serviceId;
      return this;
    }

    public Builder serviceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
      return this;
    }

    public Builder licenseToken(String licenseToken) {
      this.licenseToken = licenseToken;
      return this;
    }

    public Builder instanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
    }

    public Builder checksum(String checksum) {
      this.checksum = checksum;
      return this;
    }

    public Builder forceTokenRefresh(boolean forceTokenRefresh) {
      this.forceTokenRefresh = forceTokenRefresh;
      return this;
    }

    public LicenseServerValidationRequest build() {
      return new LicenseServerValidationRequest(
          serviceId,
          serviceVersion,
          licenseKey,
          licenseToken,
          instanceId,
          checksum,
          forceTokenRefresh);
    }
  }
}
