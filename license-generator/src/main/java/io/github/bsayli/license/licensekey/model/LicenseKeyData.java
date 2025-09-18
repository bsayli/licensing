package io.github.bsayli.license.licensekey.model;

import static io.github.bsayli.license.common.LicenseConstants.LICENSE_DELIMITER;

public record LicenseKeyData(String prefix, String opaquePayloadB64Url) {

  public String generateLicenseKey() {
    return prefix + LICENSE_DELIMITER + opaquePayloadB64Url;
  }
}
