package io.github.bsayli.licensing.api.dto;

public record LicenseValidationResponse(LicenseTokenStatus status, String licenseToken) {
  public static LicenseValidationResponse created(String token) {
    return new LicenseValidationResponse(LicenseTokenStatus.TOKEN_CREATED, token);
  }

  public static LicenseValidationResponse refreshed(String token) {
    return new LicenseValidationResponse(LicenseTokenStatus.TOKEN_REFRESHED, token);
  }

  public static LicenseValidationResponse active() {
    return new LicenseValidationResponse(LicenseTokenStatus.TOKEN_ACTIVE, null);
  }
}
