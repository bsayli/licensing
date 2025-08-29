package com.c9.licensing.sdk.cli.model;

import java.util.List;

public record LicenseValidationResponse(
    boolean success, String status, String message, List<String> errorDetails) {

  public static class Builder {
    private boolean success;
    private String status;
    private String message;
    private List<String> errorDetails;

    public Builder success(boolean success) {
      this.success = success;
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

    public LicenseValidationResponse build() {
      return new LicenseValidationResponse(success, status, message, errorDetails);
    }
  }
}
