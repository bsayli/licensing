package io.github.bsayli.license.cli.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseKeyService")
class LicenseKeyServiceTest {

  private final LicenseKeyService svc = new LicenseKeyService();

  private static SecretKey genAes(int bits) throws Exception {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(bits);
    return kg.generateKey();
  }

  @Test
  @DisplayName("generate() returns 'PREFIX.OPAQUE' and segments are consistent")
  void generate_ok() throws Exception {
    String userId = "11111111-2222-3333-4444-555555555555";
    SecretKey aesKey = genAes(256);

    var res = svc.generate(userId, aesKey);

    assertNotNull(res);
    assertNotNull(res.licenseKey());
    assertFalse(res.licenseKey().isBlank());

    String[] parts = res.licenseKey().split("\\.", 2);
    assertEquals(2, parts.length, "License key must have 2 segments: PREFIX.OPAQUE");
    assertEquals(res.prefix(), parts[0], "Prefix must match");
    assertEquals(res.opaquePayloadB64Url(), parts[1], "Opaque payload must match");

    Pattern urlSafe = Pattern.compile("^[A-Za-z0-9_-]+$");
    assertTrue(
        urlSafe.matcher(res.opaquePayloadB64Url()).matches(),
        "Opaque payload must be URL-safe Base64 (no padding)");
  }

  @Test
  @DisplayName("Blank userId should throw IllegalArgumentException")
  void blank_userId_throws() throws Exception {
    SecretKey aesKey = genAes(256);
    assertThrows(IllegalArgumentException.class, () -> svc.generate(" ", aesKey));
    assertThrows(IllegalArgumentException.class, () -> svc.generate(null, aesKey));
  }

  @Test
  @DisplayName("Null AES key should throw IllegalArgumentException")
  void null_key_throws() {
    assertThrows(
        IllegalArgumentException.class,
        () -> svc.generate("11111111-2222-3333-4444-555555555555", null));
  }
}
