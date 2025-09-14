package io.github.bsayli.license.cli.service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseTokenService")
class LicenseTokenServiceTest {

  private final LicenseTokenService svc = new LicenseTokenService();

  @Test
  @DisplayName("Blank inputs should throw IllegalArgumentException")
  void blank_inputs_throw() {
    assertThrows(IllegalArgumentException.class, () -> svc.validate(null, "t"));
    assertThrows(IllegalArgumentException.class, () -> svc.validate(" ", "t"));
    assertThrows(IllegalArgumentException.class, () -> svc.validate("pk", null));
    assertThrows(IllegalArgumentException.class, () -> svc.validate("pk", " "));
  }

  @Test
  @DisplayName("Invalid key or malformed JWT should raise an exception from extractor")
  void invalid_inputs_bubble_up() {

    String fakeSpkiB64 =
        Base64.getEncoder().encodeToString("not-a-real-spki".getBytes(StandardCharsets.UTF_8));

    String badJwt = "aaa.bbb.ccc";

    assertThrows(RuntimeException.class, () -> svc.validate(fakeSpkiB64, badJwt));
  }
}
