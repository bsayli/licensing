package io.github.bsayli.licensing.security.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.service.exception.license.LicenseInvalidException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: UserIdEncryptorImpl (opaque=version|flags|salt|gcm[iv||ct||tag])")
class UserIdEncryptorImplTest {

  private static final byte VERSION_1 = 0x01;
  private static final byte FLAGS_NONE = 0x00;
  private static final int SALT_LEN = 16;

  private static String validKeyB64(int bytes) {
    return Base64.getEncoder().encodeToString(new byte[bytes]);
  }

  private static String validKeyB64() {
    return validKeyB64(16);
  }

  private static String makeLicenseFromEncryptedUser(String encUserStdB64) {
    byte[] gcm = Base64.getDecoder().decode(encUserStdB64);

    byte[] salt = new byte[SALT_LEN];
    new SecureRandom().nextBytes(salt);

    ByteBuffer bb = ByteBuffer.allocate(2 + SALT_LEN + gcm.length);
    bb.put(VERSION_1);
    bb.put(FLAGS_NONE);
    bb.put(salt);
    bb.put(gcm);

    String opaqueUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    return "BSAYLI." + opaqueUrl;
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
  @DisplayName("extractAndDecryptUserId parses `BSAYLI.<opaqueB64Url>` and decrypts")
  void extractAndDecryptUserId_ok() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String user = "u-42";
    String encUserStdB64 = enc.encrypt(user);

    String license = makeLicenseFromEncryptedUser(encUserStdB64);
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
    String badLen = Base64.getEncoder().encodeToString(new byte[10]); // not 16/24/32
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
    String b64 = Base64.getEncoder().encodeToString(new byte[20]); // garbage
    assertThrows(LicenseInvalidException.class, () -> enc.decrypt(b64));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws on wrong prefix")
  void extract_wrong_prefix() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String encUserStdB64 = enc.encrypt("u");
    String license = makeLicenseFromEncryptedUser(encUserStdB64).replace("BSAYLI.", "WRONG.");
    assertThrows(LicenseInvalidException.class, () -> enc.extractAndDecryptUserId(license));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws on invalid format (missing '.')")
  void extract_invalid_format() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    assertThrows(LicenseInvalidException.class, () -> enc.extractAndDecryptUserId("BSAYLI"));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws on non-URL-safe opaque")
  void extract_invalid_opaque_charset() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    assertThrows(
        LicenseInvalidException.class, () -> enc.extractAndDecryptUserId("BSAYLI.AAA+BBB"));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws when opaque too short")
  void extract_opaque_too_short() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String tinyOpaque = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[8]);
    assertThrows(
        LicenseInvalidException.class, () -> enc.extractAndDecryptUserId("BSAYLI." + tinyOpaque));
  }

  @Test
  @DisplayName("encrypt returns Base64(std)")
  void encrypt_returns_base64() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String token = enc.encrypt("abc");
    assertDoesNotThrow(() -> Base64.getDecoder().decode(token));
    String raw = new String(Base64.getDecoder().decode(token), StandardCharsets.ISO_8859_1);
    assertNotNull(raw);
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws when version != 0x01")
  void extract_wrong_version() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String user = "u-42";
    String encUserStdB64 = enc.encrypt(user);

    byte[] gcm = Base64.getDecoder().decode(encUserStdB64);
    byte[] salt = new byte[SALT_LEN];
    new SecureRandom().nextBytes(salt);

    ByteBuffer bb = ByteBuffer.allocate(2 + SALT_LEN + gcm.length);
    bb.put((byte) 0x02); // wrong version
    bb.put((byte) 0x00); // flags
    bb.put(salt);
    bb.put(gcm);

    String opaqueUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    String license = "BSAYLI." + opaqueUrl;

    assertThrows(LicenseInvalidException.class, () -> enc.extractAndDecryptUserId(license));
  }

  @Test
  @DisplayName("extractAndDecryptUserId throws when flags != 0x00")
  void extract_wrong_flags() {
    var enc = new UserIdEncryptorImpl(validKeyB64());
    String user = "u-99";
    String encUserStdB64 = enc.encrypt(user);

    byte[] gcm = Base64.getDecoder().decode(encUserStdB64);
    byte[] salt = new byte[SALT_LEN];
    new SecureRandom().nextBytes(salt);

    ByteBuffer bb = ByteBuffer.allocate(2 + SALT_LEN + gcm.length);
    bb.put(VERSION_1); // correct version
    bb.put((byte) 0x01); // wrong flags
    bb.put(salt);
    bb.put(gcm);

    String opaqueUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    String license = "BSAYLI." + opaqueUrl;

    assertThrows(LicenseInvalidException.class, () -> enc.extractAndDecryptUserId(license));
  }
}
