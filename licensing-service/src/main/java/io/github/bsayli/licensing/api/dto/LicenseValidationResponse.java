package io.github.bsayli.licensing.api.dto;

public record LicenseValidationResponse(LicenseTokenStatus status, String licenseToken) {
  public static LicenseValidationResponse created(String token) {
    return new LicenseValidationResponse(LicenseTokenStatus.CREATED, token);
  }

  public static LicenseValidationResponse refreshed(String token) {
    return new LicenseValidationResponse(LicenseTokenStatus.REFRESHED, token);
  }

  public static LicenseValidationResponse active() {
    return new LicenseValidationResponse(LicenseTokenStatus.ACTIVE, null);
  }
}
