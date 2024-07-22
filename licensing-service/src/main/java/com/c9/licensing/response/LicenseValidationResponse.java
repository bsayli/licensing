package com.c9.licensing.response;

import java.util.List;

public record LicenseValidationResponse(boolean success, String token, String status, String message, List<String> errorDetails) {

    public static class Builder {
        private boolean success;
        private String token;
        private String status;
        private String message;
        private List<String> errorDetails;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
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
        
        //Validation for the fields
        public LicenseValidationResponse build() {
        	if (success && (token == null)) {
        		throw new IllegalArgumentException("When the validation is successful, you have to provide a token!");
        	}
        	if(!success && (status == null || message == null)) {
        		throw new IllegalArgumentException("When the validation is not successful, you have to provide an errorCode and a message!");
        	}
        	
        	return new LicenseValidationResponse(success, token, status, message, errorDetails);
        }
    }
}