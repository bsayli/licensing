package com.c9.licensing.model;

public record ClientInfo(String serviceId, String serviceVersion, String instanceId, String licenseToken, String encUserId, String signature, String checksum) {

	public static class Builder {
		private String serviceId;
		private String serviceVersion;
		private String instanceId;
		private String licenseToken;
		private String encUserId;
		private String signature;
		private String checksum;
		
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
		
		public Builder licenseToken(String licenseToken) {
			this.licenseToken = licenseToken;
			return this;
		}
		
		public Builder encUserId(String encUserId) {
			this.encUserId = encUserId;
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

		public ClientInfo build() {
			return new ClientInfo(serviceId, serviceVersion, instanceId, licenseToken, encUserId, signature, checksum);
		}
	}
}
