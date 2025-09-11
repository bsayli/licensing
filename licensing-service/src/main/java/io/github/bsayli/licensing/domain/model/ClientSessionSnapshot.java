package io.github.bsayli.licensing.domain.model;

import java.io.Serializable;

public record ClientSessionSnapshot(
    String licenseToken, String encUserId, String serviceId, String serviceVersion, String checksum)
    implements Serializable {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String licenseToken;
    private String encUserId;
    private String serviceId;
    private String serviceVersion;
    private String checksum;

    public Builder licenseToken(String licenseToken) {
      this.licenseToken = licenseToken;
      return this;
    }

    public Builder encUserId(String encUserId) {
      this.encUserId = encUserId;
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

    public Builder checksum(String checksum) {
      this.checksum = checksum;
      return this;
    }

    public ClientSessionSnapshot build() {
      return new ClientSessionSnapshot(
          licenseToken, encUserId, serviceId, serviceVersion, checksum);
    }
  }
}
