package io.github.bsayli.license.signature.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
  "serviceId",
  "serviceVersion",
  "licenseTokenHash",
  "encryptedLicenseKeyHash",
  "instanceId"
})
public class SignatureData {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final String serviceId;
  private final String serviceVersion;
  private final String encryptedLicenseKeyHash; // optional, XOR with licenseTokenHash
  private final String licenseTokenHash; // optional, XOR with encryptedLicenseKeyHash
  private final String instanceId;

  private SignatureData(Builder builder) {
    this.serviceId = builder.serviceId;
    this.serviceVersion = builder.serviceVersion;
    this.encryptedLicenseKeyHash = builder.encryptedLicenseKeyHash;
    this.licenseTokenHash = builder.licenseTokenHash;
    this.instanceId = builder.instanceId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getServiceId() {
    return serviceId;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public String getEncryptedLicenseKeyHash() {
    return encryptedLicenseKeyHash;
  }

  public String getLicenseTokenHash() {
    return licenseTokenHash;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String toJson() throws JsonProcessingException {
    return MAPPER.writeValueAsString(this);
  }

  public static class Builder {
    private String serviceId;
    private String serviceVersion;
    private String encryptedLicenseKeyHash;
    private String licenseTokenHash;
    private String instanceId;

    private static boolean isBlank(String s) {
      return s == null || s.isBlank();
    }

    public Builder serviceId(String v) {
      this.serviceId = v;
      return this;
    }

    public Builder serviceVersion(String v) {
      this.serviceVersion = v;
      return this;
    }

    public Builder encryptedLicenseKeyHash(String v) {
      this.encryptedLicenseKeyHash = v;
      return this;
    }

    public Builder licenseTokenHash(String v) {
      this.licenseTokenHash = v;
      return this;
    }

    public Builder instanceId(String v) {
      this.instanceId = v;
      return this;
    }

    public SignatureData build() {
      if (isBlank(serviceId) || isBlank(serviceVersion) || isBlank(instanceId)) {
        throw new IllegalStateException("serviceId, serviceVersion and instanceId are required");
      }
      boolean hasEnc = !isBlank(encryptedLicenseKeyHash);
      boolean hasTok = !isBlank(licenseTokenHash);
      if (hasEnc == hasTok) {
        throw new IllegalStateException(
            "Exactly one of encryptedLicenseKeyHash or licenseTokenHash must be set");
      }
      return new SignatureData(this);
    }
  }
}
