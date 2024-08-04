package com.c9.licensing.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignatureData {

    private final String serviceId;
    private final String serviceVersion;
    @JsonInclude(Include.NON_NULL)
    private final String encryptedLicenseKeyHash;
    @JsonInclude(Include.NON_NULL)
    private final String licenseTokenHash;
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
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
    
    public static class Builder {
        private String serviceId;
        private String serviceVersion;
        private String encryptedLicenseKeyHash;
        private String licenseTokenHash;
        private String instanceId;

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
            return new SignatureData(this);
        }
    }
}
