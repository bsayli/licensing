package io.github.bsayli.license.licensekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.B64URL_NOPAD_ENC;
import static io.github.bsayli.license.common.LicenseConstants.LICENSE_KEY_PREFIX;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKey;

public final class LicenseKeyGenerator {
  private static final byte VERSION_1 = 0x01;
  private static final byte FLAGS_NONE = 0x00;
  private static final int SALT_LEN = 16;
  private static final SecureRandom RNG = new SecureRandom();

  private LicenseKeyGenerator() {}

  public static LicenseKeyData generateLicenseKey(String userIdPlain, SecretKey aesKey) {
    try {
      byte[] opaque = buildOpaquePayload(userIdPlain, aesKey);
      String b64url = B64URL_NOPAD_ENC.encodeToString(opaque);
      return new LicenseKeyData(LICENSE_KEY_PREFIX, b64url);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate opaque license key", e);
    }
  }

  private static byte[] buildOpaquePayload(String userId, SecretKey aesKey)
      throws GeneralSecurityException {
    byte[] salt = new byte[SALT_LEN];
    RNG.nextBytes(salt);

    byte[] gcm = UserIdEncrypter.encryptRaw(userId, aesKey); // tek yol

    ByteBuffer bb = ByteBuffer.allocate(2 + SALT_LEN + gcm.length);
    bb.put(VERSION_1);
    bb.put(FLAGS_NONE);
    bb.put(salt);
    bb.put(gcm);
    return bb.array();
  }
}
