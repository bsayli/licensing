package io.github.bsayli.license.cli.service;

import static org.junit.jupiter.api.Assertions.*;

import java.security.GeneralSecurityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: UserIdCryptoService")
class UserIdCryptoServiceTest {

  private final UserIdCryptoService svc = new UserIdCryptoService();

  @Test
  @DisplayName("encrypt/decrypt round-trip should succeed")
  void roundTrip_ok() throws Exception {
    String userId = "2f0f0a2a-1111-2222-3333-abcdefabcdef";
    String enc = svc.encrypt(userId);
    assertNotNull(enc);
    assertNotEquals(userId, enc);

    String dec = svc.decrypt(enc);
    assertEquals(userId, dec);
  }

  @Test
  @DisplayName("encrypt with blank userId should throw IAE")
  void encrypt_blank_throws() {
    assertThrows(IllegalArgumentException.class, () -> svc.encrypt("  "));
    assertThrows(IllegalArgumentException.class, () -> svc.encrypt(null));
  }

  @Test
  @DisplayName("decrypt with blank ciphertext should throw IAE")
  void decrypt_blank_throws() {
    assertThrows(IllegalArgumentException.class, () -> svc.decrypt("  "));
    assertThrows(IllegalArgumentException.class, () -> svc.decrypt(null));
  }

  @Test
  @DisplayName("decrypt with malformed Base64 should throw IAE")
  void decrypt_malformedBase64_throwsIAE() {
    assertThrows(IllegalArgumentException.class, () -> svc.decrypt("not-base64!!"));
  }

  @Test
  @DisplayName("decrypt with tampered ciphertext should throw GSE")
  void decrypt_tampered_throwsGSE() throws Exception {
    String userId = "2f0f0a2a-1111-2222-3333-abcdefabcdef";
    String enc = svc.encrypt(userId);

    byte[] bytes = java.util.Base64.getDecoder().decode(enc);
    bytes[bytes.length - 1] ^= 0x01;
    String tampered = java.util.Base64.getEncoder().encodeToString(bytes);

    assertThrows(GeneralSecurityException.class, () -> svc.decrypt(tampered));
  }
}
