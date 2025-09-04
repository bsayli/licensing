package io.github.bsayli.license.licensekey.encrypter;

import static org.junit.jupiter.api.Assertions.*;

import java.security.GeneralSecurityException;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: UserIdEncrypter")
class UserIdEncrypterTest {

  @Test
  @DisplayName("AES/GCM round-trip should succeed")
  void encryptDecrypt_roundTrip_ok() throws GeneralSecurityException {
    String userId = UserIdEncrypter.SAMPLE_USER_ID;

    String encrypted = UserIdEncrypter.encrypt(userId);
    assertNotNull(encrypted);
    assertNotEquals(userId, encrypted);

    String decrypted = UserIdEncrypter.decrypt(encrypted);
    assertEquals(userId, decrypted);
  }

  @Test
  @DisplayName("Decrypting malformed Base64 should throw IllegalArgumentException")
  void decrypt_malformedBase64_shouldThrowIllegalArgument() {
    String invalid = "not-a-valid-base64";
    assertThrows(IllegalArgumentException.class, () -> UserIdEncrypter.decrypt(invalid));
  }

  @Test
  @DisplayName("Decrypting a tampered ciphertext should fail with GeneralSecurityException")
  void decrypt_tamperedCipher_shouldFail() throws GeneralSecurityException {
    String userId = UserIdEncrypter.SAMPLE_USER_ID;
    String encrypted = UserIdEncrypter.encrypt(userId);

    byte[] all = Base64.getDecoder().decode(encrypted);
    int tamperIndex = all.length - 1;
    all[tamperIndex] ^= 0x01;
    String tampered = Base64.getEncoder().encodeToString(all);

    assertThrows(GeneralSecurityException.class, () -> UserIdEncrypter.decrypt(tampered));
  }
}
