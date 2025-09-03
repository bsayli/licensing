package io.github.bsayli.license.signature.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Canonical payload used for detached signatures. Exactly one of {@code encryptedLicenseKeyHash} or
 * {@code licenseTokenHash} must be provided.
 */
@JsonInclude(Include.NON_NULL)
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

    public Builder serviceId(String serviceId) {
      this.serviceId = serviceId;
      return this;
    }

    public Builder serviceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
      return this;
    }

    public Builder encryptedLicenseKeyHash(String encryptedLicenseKeyHash) {
      this.encryptedLicenseKeyHash = encryptedLicenseKeyHash;
      return this;
    }

    public Builder licenseTokenHash(String licenseTokenHash) {
      this.licenseTokenHash = licenseTokenHash;
      return this;
    }

    public Builder instanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
    }

    public SignatureData build() {
      // required fields
      if (isBlank(serviceId) || isBlank(serviceVersion) || isBlank(instanceId)) {
        throw new IllegalStateException("serviceId, serviceVersion and instanceId are required");
      }

      // XOR rule: exactly one of the hashes must be present
      boolean hasEncKeyHash = !isBlank(encryptedLicenseKeyHash);
      boolean hasTokenHash = !isBlank(licenseTokenHash);
      if (hasEncKeyHash == hasTokenHash) {
        throw new IllegalStateException(
            "Exactly one of encryptedLicenseKeyHash or licenseTokenHash must be set");
      }

      return new SignatureData(this);
    }
  }
}
