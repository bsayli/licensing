package io.github.bsayli.licensing.service.token;

import io.github.bsayli.licensing.domain.result.LicenseValidationResult;

public record LicenseTokenIssueRequest(
    String clientId,
    LicenseValidationResult result,
    String serviceId,
    String serviceVersion,
    String instanceId,
    String checksum,
    String signature) {

  public static class Builder {
    private String clientId;
    private LicenseValidationResult result;
    private String serviceId;
    private String serviceVersion;
    private String instanceId;
    private String checksum;
    private String signature;

    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder result(LicenseValidationResult result) {
      this.result = result;
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

    public Builder instanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
    }

    public Builder checksum(String checksum) {
      this.checksum = checksum;
      return this;
    }

    public Builder signature(String signature) {
      this.signature = signature;
      return this;
    }

    public LicenseTokenIssueRequest build() {
      return new LicenseTokenIssueRequest(
          clientId, result, serviceId, serviceVersion, instanceId, checksum, signature);
    }
  }
}
