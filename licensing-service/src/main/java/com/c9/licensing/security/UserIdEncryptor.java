package com.c9.licensing.security;

import com.c9.licensing.model.errors.LicenseInvalidException;

public interface UserIdEncryptor {

  String MESSAGE_LICENSE_KEY_INVALID = "License Key Invalid!";

  String LICENSEKEYPREFIX = "C9INE";
  String ALGORITHM = "AES/GCM/NoPadding";
  int GCM_IV_LENGTH = 12; // Recommended for GCM
  int GCM_TAG_LENGTH = 16; // 128 bits
  String DELIMITER = "~";

  public String extractAndDecryptUserId(String licenseKey) throws LicenseInvalidException;

  public String encrypt(String userId) throws LicenseInvalidException;

  public String decrypt(String userId) throws LicenseInvalidException;
}
