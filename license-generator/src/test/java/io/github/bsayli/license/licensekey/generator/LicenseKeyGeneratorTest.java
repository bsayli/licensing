package io.github.bsayli.license.licensekey.generator;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.LicenseConstants;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseKeyGenerator")
class LicenseKeyGeneratorTest {

  @Test
  @DisplayName("Generated license key should follow PREFIX~random~encryptedUserId format")
  void generateLicenseKey_format_ok() {
    String encryptedUserId = "encryptedUserId123";
    LicenseKeyData keyData = LicenseKeyGenerator.generateLicenseKey(encryptedUserId);
    String licenseKey = keyData.generateLicenseKey();

    String[] parts = licenseKey.split(LicenseConstants.LICENSE_DELIMITER);

    assertEquals(3, parts.length, "License key must have 3 segments");
    assertEquals(LicenseConstants.LICENSE_KEY_PREFIX, parts[0]);
    assertEquals(encryptedUserId, parts[2]);
    assertFalse(parts[1].isBlank(), "Random segment must not be blank");
  }

  @Test
  @DisplayName("Random segment should differ across multiple invocations")
  void randomSegment_shouldBeDifferent() {
    String encryptedUserId = "encryptedUserId123";

    Set<String> randomSegments = new HashSet<>();
    for (int i = 0; i < 5; i++) {
      LicenseKeyData keyData = LicenseKeyGenerator.generateLicenseKey(encryptedUserId);
      String[] parts = keyData.generateLicenseKey().split(LicenseConstants.LICENSE_DELIMITER);
      randomSegments.add(parts[1]);
    }

    assertTrue(randomSegments.size() > 1, "Random segment must vary across generations");
  }

  @Test
  @DisplayName("Random segment is URL-safe Base64 (no '+', '/', '=') and has expected length")
  void randomSegment_urlSafe_and_expectedLength() {
    String encryptedUserId = "enc-uid";
    LicenseKeyData keyData = LicenseKeyGenerator.generateLicenseKey(encryptedUserId);
    String[] parts = keyData.generateLicenseKey().split(LicenseConstants.LICENSE_DELIMITER);
    String random = parts[1];

    Pattern urlSafe = Pattern.compile("^[A-Za-z0-9_-]+$");
    assertTrue(urlSafe.matcher(random).matches(), "Random segment must be URL-safe Base64");

    int n = LicenseConstants.RANDOM_BYTES_FOR_KEY;
    int paddedLen = 4 * ((n + 2) / 3);
    int paddingChars = (3 - (n % 3)) % 3;
    int expectedLen = paddedLen - paddingChars;

    assertEquals(
        expectedLen, random.length(), "Random segment length should match expected Base64 length");
  }

  @Test
  @DisplayName("Many generated keys should be unique (probabilistic check)")
  void generate_many_shouldBeUnique() {
    String encryptedUserId = "enc-uid";
    int count = 100;
    Set<String> keys = new HashSet<>(count);

    for (int i = 0; i < count; i++) {
      LicenseKeyData keyData = LicenseKeyGenerator.generateLicenseKey(encryptedUserId);
      keys.add(keyData.generateLicenseKey());
    }

    assertEquals(count, keys.size(), "Generated license keys should be unique in a larger sample");
  }
}
