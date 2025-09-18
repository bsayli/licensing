package io.github.bsayli.licensing.security.impl;

import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;
import java.nio.ByteBuffer;
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
  private static final int VERSION_LEN = 1;
  private static final int FLAGS_LEN = 1;
  private static final int SALT_LEN = 16;
  private static final int HEADER_LEN = VERSION_LEN + FLAGS_LEN + SALT_LEN;

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
    if (licenseKey == null || licenseKey.isBlank()) throw new LicenseInvalidException();

    String[] parts = licenseKey.split("\\.", 2);
    if (parts.length != 2 || !LICENSE_KEY_PREFIX.equals(parts[0]))
      throw new LicenseInvalidException();

    String opaqueB64Url = parts[1];
    if (!opaqueB64Url.matches("^[A-Za-z0-9_-]+$")) throw new LicenseInvalidException();

    final byte[] opaque;
    try {
      opaque = Base64.getUrlDecoder().decode(opaqueB64Url);
    } catch (IllegalArgumentException e) {
      throw new LicenseInvalidException(e);
    }

    if (opaque.length < HEADER_LEN + GCM_IV_LENGTH + GCM_TAG_LENGTH)
      throw new LicenseInvalidException();

    ByteBuffer bb = ByteBuffer.wrap(opaque);
    byte version = bb.get();
    byte flags = bb.get();

    if (version != 0x01 || flags != 0x00) {
      throw new LicenseInvalidException();
    }

    byte[] salt = new byte[SALT_LEN];
    bb.get(salt);

    byte[] gcm = new byte[bb.remaining()];
    bb.get(gcm);

    if (gcm.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) throw new LicenseInvalidException();

    byte[] iv = Arrays.copyOfRange(gcm, 0, GCM_IV_LENGTH);
    byte[] ctTag = Arrays.copyOfRange(gcm, GCM_IV_LENGTH, gcm.length);

    try {
      Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
      byte[] decryptedBytes = cipher.doFinal(ctTag);
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new LicenseInvalidException(e);
    }
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
    if (decoded.length <= GCM_IV_LENGTH) throw new LicenseInvalidException();

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
