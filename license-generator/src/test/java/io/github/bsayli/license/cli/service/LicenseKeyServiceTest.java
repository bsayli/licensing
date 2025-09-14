package io.github.bsayli.license.cli.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.LicenseConstants;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseKeyService")
class LicenseKeyServiceTest {

  private final LicenseKeyService svc = new LicenseKeyService();

  @Test
  @DisplayName("generate() should return a 3-part key and consistent segments")
  void generate_ok() throws GeneralSecurityException {
    String userId = "11111111-2222-3333-4444-555555555555";

    var res = svc.generate(userId);

    assertNotNull(res);
    assertNotNull(res.licenseKey());
    assertFalse(res.licenseKey().isBlank());

    String[] parts = res.licenseKey().split(LicenseConstants.LICENSE_DELIMITER);
    assertEquals(3, parts.length, "License key must have 3 segments");
    assertEquals(res.prefix(), parts[0]);
    assertEquals(res.randomString(), parts[1]);
    assertEquals(res.encryptedUserId(), parts[2]);

    Pattern urlSafe = Pattern.compile("^[A-Za-z0-9_-]+$");
    assertTrue(urlSafe.matcher(res.randomString()).matches(), "Random must be URL-safe Base64");
  }

  @Test
  @DisplayName("Blank userId should throw IllegalArgumentException")
  void blank_userId_throws() {
    assertThrows(IllegalArgumentException.class, () -> svc.generate(" "));
    assertThrows(IllegalArgumentException.class, () -> svc.generate(null));
  }
}
