package io.github.bsayli.license.licensekey.model;

import io.github.bsayli.license.common.LicenseConstants;

public record LicenseKeyData(String prefix, String randomString, String uuid) {

  public String generateLicenseKey() {
    return prefix
        + LicenseConstants.LICENSE_DELIMITER
        + randomString
        + LicenseConstants.LICENSE_DELIMITER
        + uuid;
  }
}
