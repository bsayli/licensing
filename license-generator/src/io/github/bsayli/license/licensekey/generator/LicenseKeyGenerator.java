package io.github.bsayli.license.licensekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.RNG;
import static io.github.bsayli.license.common.LicenseConstants.LICENSE_KEY_PREFIX;
import static io.github.bsayli.license.common.LicenseConstants.RANDOM_BYTES_FOR_KEY;
import static io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter.SAMPLE_USER_ID;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a license key string in the format: PREFIX ~ RANDOM_URLSAFE_BASE64 ~ ENCRYPTED_USER_ID
 *
 * <p>Recommended flow with Keycloak:
 *
 * <ol>
 *   <li>Obtain the Keycloak user id (UUID) after user registration/auth.
 *   <li>Encrypt that UUID using {@link UserIdEncrypter#encrypt(String)} (AES/GCM).
 *   <li>Embed the encrypted value in the license string using {@link #generateLicenseKey(String)}.
 * </ol>
 *
 * <p>This ensures each license is uniquely and verifiably bound to a Keycloak user, while not
 * exposing the raw UUID.
 */
public class LicenseKeyGenerator {

  private static final Logger log = LoggerFactory.getLogger(LicenseKeyGenerator.class);

  public static void main(String[] args) throws Exception {
    // In production: fetch the real Keycloak user id (UUID) from your identity flow.
    // Encrypt Keycloak user id (AES/GCM) before embedding into the license string.
    String encryptedUserId = UserIdEncrypter.encrypt(SAMPLE_USER_ID);
    LicenseKeyData key = generateLicenseKey(encryptedUserId);
    log.info("Generated License Key: {}", key.generateLicenseKey());
  }

  /**
   * Generates a license key by combining:
   *
   * <pre>
   *   PREFIX ~ urlsafeRandom({@value io.github.bsayli.license.common.LicenseConstants#RANDOM_BYTES_FOR_KEY}) ~ encryptedUserId
   * </pre>
   *
   * @param encryptedUserId AES/GCM-encrypted Keycloak user id (UUID)
   * @return immutable DTO holding the 3 segments
   */
  public static LicenseKeyData generateLicenseKey(String encryptedUserId) {
    String randomSegment = urlSafeRandom(RANDOM_BYTES_FOR_KEY);
    return new LicenseKeyData(LICENSE_KEY_PREFIX, randomSegment, encryptedUserId);
  }

  private static String urlSafeRandom(int numBytes) {
    byte[] buf = new byte[numBytes];
    RNG.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}
