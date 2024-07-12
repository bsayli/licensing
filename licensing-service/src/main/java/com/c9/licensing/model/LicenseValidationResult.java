package com.c9.licensing.model;

public record LicenseValidationResult(String userId, String appInstanceId, boolean valid, String licenseTier, String licenseStatus, LicenseErrorCode errorCode, String message) {

    // Builder for LicenseValidationResult
    public static class Builder {
        private String userId;
        private String appInstanceId;
        private boolean valid;
        private String licenseTier;
        private String licenseStatus;
        private LicenseErrorCode errorCode;
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
        
        public Builder errorCode(LicenseErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        // Default value for valid is true
        public LicenseValidationResult build() {
            return new LicenseValidationResult(userId, appInstanceId, valid, licenseTier, licenseStatus, errorCode, message);
        }
    }
}
