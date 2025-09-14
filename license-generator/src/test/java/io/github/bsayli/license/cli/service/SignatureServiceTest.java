package io.github.bsayli.license.cli.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureService")
class SignatureServiceTest {

  private final SignatureService svc = new SignatureService();

  @Test
  @DisplayName("Blank inputs should throw IllegalArgumentException")
  void blank_inputs_throw() {
    assertThrows(
        IllegalArgumentException.class, () -> svc.signWithLicenseKey("", "v", "i", "k", "pk"));
    assertThrows(IllegalArgumentException.class, () -> svc.signWithToken("s", "", "i", "t", "pk"));
    assertThrows(IllegalArgumentException.class, () -> svc.verify("", "{}", "sig"));
  }

  @Test
  @DisplayName("Invalid keys should bubble up as GeneralSecurityException from sign/verify")
  void invalid_keys_bubble_up() {
    String fakePriv = "not-a-real-pkcs8";
    String fakePub = "not-a-real-spki";

    assertThrows(
        IllegalArgumentException.class, () -> svc.signWithLicenseKey("s", "v", "i", "K", fakePriv));

    assertThrows(
        IllegalArgumentException.class, () -> svc.signWithToken("s", "v", "i", "T", fakePriv));

    assertThrows(IllegalArgumentException.class, () -> svc.verify(fakePub, "{}", "sig"));
  }
}
