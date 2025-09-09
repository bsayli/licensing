package io.github.bsayli.licensing.security;

import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;

public interface UserIdEncryptor {

  String extractAndDecryptUserId(String licenseKey) throws LicenseInvalidException;

  String encrypt(String userId) throws LicenseInvalidException;

  String decrypt(String userId) throws LicenseInvalidException;
}
