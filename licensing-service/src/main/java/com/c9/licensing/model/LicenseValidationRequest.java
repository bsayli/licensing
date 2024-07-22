package com.c9.licensing.model;

public record LicenseValidationRequest(String serviceId, String licenseKey, String licenseToken, String instanceId,
		String signature, String checksum) {

	public static class Builder {
		private String licenseKey;
		private String serviceId;
		private String licenseToken;
		private String instanceId;
		private String signature;
		private String checksum;

		public Builder licenseKey(String licenseKey) {
			this.licenseKey = licenseKey;
			return this;
		}

		public Builder serviceId(String serviceId) {
			this.serviceId = serviceId;
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

		public Builder signature(String signature) {
			this.signature = signature;
			return this;
		}

		public Builder checksum(String checksum) {
			this.checksum = checksum;
			return this;
		}

		public LicenseValidationRequest build() {
			return new LicenseValidationRequest(serviceId, licenseKey, licenseToken, instanceId, signature, checksum);
		}
	}
}
