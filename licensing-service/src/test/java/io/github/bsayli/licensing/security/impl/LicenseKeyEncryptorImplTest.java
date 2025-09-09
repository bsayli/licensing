package io.github.bsayli.licensing.security.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseKeyEncryptorImpl")
class LicenseKeyEncryptorImplTest {

  private static String keyB64(int seed) {
    byte[] k = new byte[16];
    for (int i = 0; i < k.length; i++) k[i] = (byte) (seed + i);
    return Base64.getEncoder().encodeToString(k);
  }

  @Test
  @DisplayName("encrypt/decrypt round-trip returns original plaintext")
  void roundTrip_ok() {
    String key = keyB64(1);
    var enc = new LicenseKeyEncryptorImpl(key);
    String plain = "hello-şĞİçöüğ-🙂-LICENSE";
    String ct = enc.encrypt(plain);
    String back = enc.decrypt(ct);
    assertEquals(plain, back);
  }

  @Test
  @DisplayName("encrypt with random IV yields different ciphertexts for same plaintext")
  void randomIv_diffCiphertexts() {
    String key = keyB64(7);
    var enc = new LicenseKeyEncryptorImpl(key);
    String plain = "same-text";
    String c1 = enc.encrypt(plain);
    String c2 = enc.encrypt(plain);
    assertNotEquals(c1, c2);
    assertEquals(plain, enc.decrypt(c1));
    assertEquals(plain, enc.decrypt(c2));
  }

  @Test
  @DisplayName("decrypt throws for invalid Base64 input")
  void decrypt_invalidBase64_throws() {
    String key = keyB64(3);
    var enc = new LicenseKeyEncryptorImpl(key);
    assertThrows(LicenseInvalidException.class, () -> enc.decrypt("***not-base64***"));
  }

  @Test
  @DisplayName("decrypt throws for input shorter than IV")
  void decrypt_tooShort_throws() {
    String key = keyB64(4);
    var enc = new LicenseKeyEncryptorImpl(key);
    String tooShort = Base64.getEncoder().encodeToString(new byte[8]);
    assertThrows(LicenseInvalidException.class, () -> enc.decrypt(tooShort));
  }

  @Test
  @DisplayName("decrypt with wrong key throws")
  void decrypt_wrongKey_throws() {
    String key1 = keyB64(10);
    String key2 = keyB64(20);
    var enc1 = new LicenseKeyEncryptorImpl(key1);
    var enc2 = new LicenseKeyEncryptorImpl(key2);
    String ct = enc1.encrypt("secret");
    assertThrows(LicenseInvalidException.class, () -> enc2.decrypt(ct));
  }

  @Test
  @DisplayName("constructor throws for invalid secret key Base64")
  void constructor_invalidKey_throws() {
    assertThrows(LicenseInvalidException.class, () -> new LicenseKeyEncryptorImpl("$$$"));
  }
}
