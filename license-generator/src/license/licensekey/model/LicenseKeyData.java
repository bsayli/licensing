package license.licensekey.model;

public record LicenseKeyData(String prefix, String randomString, String uuid) {

  private static final String DELIMITER = "~";

  public String generateLicenseKey() {
    return prefix + DELIMITER + randomString + DELIMITER + uuid;
  }
}
