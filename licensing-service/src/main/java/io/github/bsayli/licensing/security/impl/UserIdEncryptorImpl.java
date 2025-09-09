package io.github.bsayli.licensing.security.impl;

import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class UserIdEncryptorImpl implements UserIdEncryptor {

  private static final String LICENSE_KEY_PREFIX = "BSAYLI";
  private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 16;
  private static final String DELIMITER = "~";

  private final SecretKey secretKey;
  private final SecureRandom secureRandom = new SecureRandom();

  public UserIdEncryptorImpl(String encodedSecretKey) {
    try {
      byte[] decodedKey = Base64.getDecoder().decode(encodedSecretKey);
      if (!(decodedKey.length == 16 || decodedKey.length == 24 || decodedKey.length == 32)) {
        throw new IllegalArgumentException("Invalid AES key length: " + decodedKey.length);
      }
      this.secretKey = new SecretKeySpec(decodedKey, KEY_ALGORITHM);
    } catch (IllegalArgumentException e) {
      throw new LicenseInvalidException(e);
    }
  }

  private static byte[] concat(byte[] a, byte[] b) {
    byte[] out = new byte[a.length + b.length];
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }

  @Override
  public String extractAndDecryptUserId(String licenseKey) throws LicenseInvalidException {
    String[] components = licenseKey.split(DELIMITER, -1);
    if (components.length != 3 || !LICENSE_KEY_PREFIX.equals(components[0])) {
      throw new LicenseInvalidException();
    }
    return decrypt(components[2]);
  }

  @Override
  public String encrypt(String plainText) throws LicenseInvalidException {
    byte[] iv = new byte[GCM_IV_LENGTH];
    secureRandom.nextBytes(iv);
    try {
      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

      byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      byte[] finalCipherText = concat(iv, cipherTextBytes);
      return Base64.getEncoder().encodeToString(finalCipherText);
    } catch (Exception e) {
      throw new LicenseInvalidException(e);
    }
  }

  @Override
  public String decrypt(String encryptedText) throws LicenseInvalidException {
    final byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(encryptedText);
    } catch (IllegalArgumentException e) {
      throw new LicenseInvalidException(e);
    }

    if (decoded.length <= GCM_IV_LENGTH) {
      throw new LicenseInvalidException();
    }

    try {
      byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
      byte[] cipherTextBytes = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new LicenseInvalidException(e);
    }
  }
}
