package com.c9.licensing.model.response;

import java.util.List;

public record LicenseValidationResponse(boolean success, String status, String message, String licenseToken, List<String> errorDetails) {

	public static class Builder {
		private boolean success;
		private String licenseToken;
		private String status;
		private String message;
		private List<String> errorDetails;

		public Builder success(boolean success) {
			this.success = success;
			return this;
		}

		public Builder licenseToken(String licenseToken) {
			this.licenseToken = licenseToken;
			return this;
		}

		public Builder status(String status) {
			this.status = status;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder errorDetails(List<String> errorDetails) {
			this.errorDetails = errorDetails;
			return this;
		}

		// Validation for the fields
		public LicenseValidationResponse build() {
			if (success && (licenseToken == null)) {
				throw new IllegalArgumentException("When the validation is successful, you have to provide a token!");
			}
			if (!success && (status == null || message == null)) {
				throw new IllegalArgumentException(
						"When the validation is not successful, you have to provide an errorCode and a message!");
			}

			return new LicenseValidationResponse(success, status, message, licenseToken, errorDetails);
		}
	}
}