package io.github.bsayli.licensing.security;

import io.github.bsayli.licensing.model.errors.LicenseInvalidException;

public interface LicenseKeyEncryptor {

  String MESSAGE_LICENSE_KEY_INVALID = "License Key Invalid!";

  String ALGORITHM = "AES/GCM/NoPadding";
  int GCM_IV_LENGTH = 12; // Recommended for GCM
  int GCM_TAG_LENGTH = 16; // 128 bits

  public String encrypt(String licenseCode) throws LicenseInvalidException;

  public String decrypt(String encryptedLicenseCode) throws LicenseInvalidException;
}
