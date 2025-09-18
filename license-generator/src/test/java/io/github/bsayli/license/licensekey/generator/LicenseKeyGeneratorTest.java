package io.github.bsayli.license.licensekey.generator;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.LicenseConstants;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseKeyGenerator")
class LicenseKeyGeneratorTest {

  private static SecretKey genAes(int bits) throws Exception {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(bits);
    return kg.generateKey();
  }

  @Test
  @DisplayName("Generated license key should follow 'PREFIX.OPAQUE' format")
  void generateLicenseKey_format_ok() throws Exception {
    String userId = "11111111-2222-3333-4444-555555555555";
    SecretKey aesKey = genAes(256);

    LicenseKeyData keyData = LicenseKeyGenerator.generateLicenseKey(userId, aesKey);
    String licenseKey = keyData.generateLicenseKey();

    assertNotNull(licenseKey);
    assertFalse(licenseKey.isBlank());

    String[] parts = licenseKey.split("\\.", 2);
    assertEquals(2, parts.length, "License key must have 2 segments: PREFIX.OPAQUE");
    assertEquals(LicenseConstants.LICENSE_KEY_PREFIX, parts[0], "Prefix must match");

    String opaque = parts[1];
    assertNotNull(opaque);
    assertFalse(opaque.isBlank());

    // URL-safe Base64 (no padding)
    assertTrue(
        opaque.matches("^[A-Za-z0-9_-]+$"), "Opaque payload must be URL-safe Base64 (no padding)");
  }

  @Test
  @DisplayName("Opaque payload must be URL-safe Base64 (no '+', '/', '=')")
  void opaque_is_urlsafe_base64() throws Exception {
    String userId = "enc-uid";
    SecretKey aesKey = genAes(256);

    String opaque = LicenseKeyGenerator.generateLicenseKey(userId, aesKey).opaquePayloadB64Url();
    assertNotNull(opaque);
    assertFalse(opaque.isBlank());
    assertTrue(
        opaque.matches("^[A-Za-z0-9_-]+$"), "Opaque payload must be URL-safe Base64 (no padding)");
  }

  @Test
  @DisplayName("Many generated keys should be unique (probabilistic check)")
  void generate_many_shouldBeUnique() throws Exception {
    String userId = "enc-uid";
    SecretKey aesKey = genAes(256);

    int count = 100;
    Set<String> keys = new HashSet<>(count);
    for (int i = 0; i < count; i++) {
      LicenseKeyData kd = LicenseKeyGenerator.generateLicenseKey(userId, aesKey);
      keys.add(kd.generateLicenseKey());
    }
    assertEquals(count, keys.size(), "Generated license keys should be unique in a larger sample");
  }
}
