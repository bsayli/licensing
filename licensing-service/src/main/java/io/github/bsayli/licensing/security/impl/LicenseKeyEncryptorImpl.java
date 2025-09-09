package io.github.bsayli.licensing.security.impl;

import io.github.bsayli.licensing.security.LicenseKeyEncryptor;
import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays; // JDK Arrays
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LicenseKeyEncryptorImpl implements LicenseKeyEncryptor {

  private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int IV_LENGTH_BYTES = 12;
  private static final int TAG_LENGTH_BITS = 128;

  private final SecretKey secretKey;
  private final SecureRandom secureRandom = new SecureRandom();

  public LicenseKeyEncryptorImpl(String encodedSecretKey) {
    try {
      byte[] decodedKey = Base64.getDecoder().decode(encodedSecretKey);
      if (!(decodedKey.length == 16 || decodedKey.length == 24 || decodedKey.length == 32)) {
        throw new LicenseInvalidException(
            new IllegalArgumentException("AES key must be 16, 24, or 32 bytes."));
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
  public String encrypt(String plainText) throws LicenseInvalidException {
    byte[] iv = new byte[IV_LENGTH_BYTES];
    secureRandom.nextBytes(iv);

    try {
      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION); // SunJCE
      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

      byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      byte[] ivPlusCipher = concat(iv, cipherText);
      return Base64.getEncoder().encodeToString(ivPlusCipher);
    } catch (Exception e) {
      throw new LicenseInvalidException(e);
    }
  }

  @Override
  public String decrypt(String encryptedText) throws LicenseInvalidException {
    final byte[] ivPlusCipher;
    try {
      ivPlusCipher = Base64.getDecoder().decode(encryptedText);
    } catch (IllegalArgumentException e) {
      throw new LicenseInvalidException(e);
    }

    if (ivPlusCipher.length <= IV_LENGTH_BYTES) {
      throw new LicenseInvalidException();
    }

    try {
      byte[] iv = Arrays.copyOfRange(ivPlusCipher, 0, IV_LENGTH_BYTES);
      byte[] cipherBytes = Arrays.copyOfRange(ivPlusCipher, IV_LENGTH_BYTES, ivPlusCipher.length);

      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

      byte[] plainBytes = cipher.doFinal(cipherBytes);
      return new String(plainBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new LicenseInvalidException(e);
    }
  }
}
