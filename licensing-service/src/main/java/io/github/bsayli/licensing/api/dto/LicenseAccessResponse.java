package io.github.bsayli.licensing.api.dto;

public record LicenseAccessResponse(LicenseAccessStatus status, String licenseToken) {
  public static LicenseAccessResponse created(String token) {
    return new LicenseAccessResponse(LicenseAccessStatus.TOKEN_CREATED, token);
  }

  public static LicenseAccessResponse refreshed(String token) {
    return new LicenseAccessResponse(LicenseAccessStatus.TOKEN_REFRESHED, token);
  }

  public static LicenseAccessResponse active(String token) {
    return new LicenseAccessResponse(LicenseAccessStatus.TOKEN_ACTIVE, token);
  }

  public static LicenseAccessResponse active() {
    return new LicenseAccessResponse(LicenseAccessStatus.TOKEN_ACTIVE, null);
  }
}
