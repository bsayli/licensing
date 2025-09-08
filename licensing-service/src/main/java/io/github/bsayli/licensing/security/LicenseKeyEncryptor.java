package io.github.bsayli.licensing.security;

import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;

public interface LicenseKeyEncryptor {
  String encrypt(String licenseCode) throws LicenseInvalidException;

  String decrypt(String encryptedLicenseCode) throws LicenseInvalidException;
}
