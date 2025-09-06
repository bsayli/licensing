package io.github.bsayli.licensing.model;

import java.io.Serializable;

public class ClientCachedLicenseData implements Serializable {

  private static final long serialVersionUID = 166852228735903999L;

  private final String licenseToken;
  private final String encUserId;
  private final String serviceId;
  private final String serviceVersion;
  private final String checksum;

  private ClientCachedLicenseData(Builder builder) {
    this.licenseToken = builder.licenseToken;
    this.encUserId = builder.encUserId;
    this.serviceId = builder.serviceId;
    this.serviceVersion = builder.serviceVersion;
    this.checksum = builder.checksum;
  }

  public String getLicenseToken() {
    return licenseToken;
  }

  public String getEncUserId() {
    return encUserId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public String getChecksum() {
    return checksum;
  }

  public static class Builder {
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

    public ClientCachedLicenseData build() {
      return new ClientCachedLicenseData(this);
    }
  }
}
