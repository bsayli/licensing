package io.github.bsayli.license.licensekey.encrypter;

import static io.github.bsayli.license.common.CryptoUtils.*;

import java.security.GeneralSecurityException;
import java.security.Security;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to encrypt/decrypt a user id with AES/GCM.
 *
 * <p>Output format: Base64( IV(12 bytes) || CIPHERTEXT )
 *
 * <p><strong>Security note:</strong> SECRET_KEY_BASE64 is inline for demo usage. In production,
 * load it from a secure source (vault/KMS/env).
 */
public final class UserIdEncrypter {

  /**
   * Demo sample UUID from a Keycloak user record.
   * <p>In real usage: fetch the actual Keycloak userId (UUID) after registration/auth.</p>
   */
  public static final String SAMPLE_USER_ID = "ba035b3e-d8b6-4a09-89c7-ab0459f2585b";

  private static final Logger LOG = LoggerFactory.getLogger(UserIdEncrypter.class);

  // Demo key — replace at runtime in real deployments.
  private static final String SECRET_KEY_BASE64 =
          "s/PUyV6Ym/v0A+HehYFZ9GTpatSL1MkcVW1zBLf3cX4=";
  private static final SecretKey ENCRYPTION_KEY = loadAesKeyFromBase64(SECRET_KEY_BASE64);

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private UserIdEncrypter() {
    // utility
  }

  /** Encrypts the given user id string with AES/GCM. */
  public static String encrypt(String userId) throws GeneralSecurityException {
    return aesGcmEncryptToBase64(ENCRYPTION_KEY, userId);
  }

  /** Decrypts a previously encrypted user id string. */
  public static String decrypt(String encryptedBase64) throws GeneralSecurityException {
    return aesGcmDecryptFromBase64(ENCRYPTION_KEY, encryptedBase64);
  }

  public static void main(String[] args) throws GeneralSecurityException {
    LOG.info("User Id (plain / from Keycloak UUID): {}", SAMPLE_USER_ID);

    String enc = encrypt(SAMPLE_USER_ID);
    LOG.info("Encrypted: {}", enc);

    String dec = decrypt(enc);
    LOG.info("Decrypted: {}", dec);
  }
}