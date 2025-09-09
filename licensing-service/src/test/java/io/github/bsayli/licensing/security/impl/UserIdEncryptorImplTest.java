package io.github.bsayli.licensing.security.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: UserIdEncryptorImpl")
class UserIdEncryptorImplTest {

  private static String validKeyB64() {
    return Base64.getEncoder().encodeToString(new byte[16]);
  }

  @Test
  @DisplayName("encrypt -> decrypt returns original")
  void encrypt_then_decrypt_roundtrip() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String plain = "user-123";
    String token = enc.encrypt(plain);
    String out = enc.decrypt(token);
    assertEquals(plain, out);
  }

  @Test
  @DisplayName("extractAndDecryptUserId parses license format and decrypts")
  void extractAndDecryptUserId_ok() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String user = "u-42";
    String encUser = enc.encrypt(user);
    String license = "C9INE~meta~" + encUser;
    String out = enc.extractAndDecryptUserId(license);
    assertEquals(user, out);
  }

  @Test
  @DisplayName("constructor throws on invalid base64 key")
  void constructor_invalid_base64_key() {
    assertThrows(LicenseInvalidException.class, () -> new UserIdEncryptorImpl("%%%"));
  }

  @Test
  @DisplayName("constructor throws on invalid key length")
  void constructor_invalid_key_length() {
    String badLen = Base64.getEncoder().encodeToString(new byte[10]);
    assertThrows(LicenseInvalidException.class, () -> new UserIdEncryptorImpl(badLen));
  }

  @Test
  @DisplayName("decrypt throws on invalid base64 token")
  void decrypt_invalid_base64() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    assertThrows(LicenseInvalidException.class, () -> enc.decrypt("not-base64"));
  }

  @Test
  @DisplayName("decrypt throws when data shorter than IV")
  void decrypt_too_short() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String b64 = Base64.getEncoder().encodeToString(new byte[8]);
    assertThrows(LicenseInvalidException.class, () -> enc.decrypt(b64));
  }

  @Test
  @DisplayName("decrypt throws on AEAD failure")
  void decrypt_aead_failure() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String b64 = Base64.getEncoder().encodeToString(new byte[20]);
    assertThrows(LicenseInvalidException.class, () -> enc.decrypt(b64));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws on wrong prefix")
  void extract_wrong_prefix() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String token = enc.encrypt("u");
    String license = "WRONG~x~" + token;
    assertThrows(LicenseInvalidException.class, () -> enc.extractAndDecryptUserId(license));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws on invalid parts")
  void extract_invalid_parts() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    assertThrows(LicenseInvalidException.class, () -> enc.extractAndDecryptUserId("C9INE~only"));
  }

  @Test
  @DisplayName("encrypt produces base64 string")
  void encrypt_returns_base64() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String token = enc.encrypt("abc");
    assertDoesNotThrow(
        () -> Base64.getDecoder().decode(token).toString().getBytes(StandardCharsets.UTF_8));
  }
}
