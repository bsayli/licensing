package com.c9.licensing.sdk.model;

public record LicenseValidationRequest(String serviceId, String serviceVersion, String licenseKey, String instanceId,
		String checksum) {

	public static class Builder {
		private String licenseKey;
		private String serviceId;
		private String serviceVersion;
		private String instanceId;
		private String checksum;

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

		public Builder instanceId(String instanceId) {
			this.instanceId = instanceId;
			return this;
		}

		public Builder checksum(String checksum) {
			this.checksum = checksum;
			return this;
		}

		public LicenseValidationRequest build() {
			return new LicenseValidationRequest(serviceId, serviceVersion, licenseKey, instanceId, checksum);
		}
	}
}
