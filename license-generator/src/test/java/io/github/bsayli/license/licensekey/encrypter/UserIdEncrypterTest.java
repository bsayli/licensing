package io.github.bsayli.license.licensekey.encrypter;

import static io.github.bsayli.license.common.CryptoUtils.loadAesKeyFromBase64;
import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.CryptoConstants;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: UserIdEncrypter (raw AES/GCM)")
class UserIdEncrypterTest {

  // 256-bit AES key (test purpose only)
  private static final String KEY_B64 = "wL6U5l5CwzE0bU3x3Z9o6v7S0X7z4k1kqQx9m8k9c0w=";

  @Test
  @DisplayName("AES/GCM raw round-trip should return original userId")
  void encryptDecrypt_roundTrip_ok() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    String userId = "ba035b3e-d8b6-4a09-89c7-ab0459f2585b";

    byte[] encrypted = UserIdEncrypter.encryptRaw(userId, key);
    assertNotNull(encrypted);
    assertTrue(
        encrypted.length > CryptoConstants.GCM_IV_LENGTH_BYTES,
        "ciphertext must contain IV + CT+TAG");

    String decrypted = UserIdEncrypter.decryptRaw(encrypted, key);
    assertEquals(userId, decrypted);
  }

  @Test
  @DisplayName("Decrypting too-short payload should throw IllegalArgumentException")
  void decrypt_invalidPayload_shouldThrowIllegalArgument() {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    // shorter than IV (12) + TAG (16) = 28 bytes
    byte[] invalid = new byte[10];
    assertThrows(IllegalArgumentException.class, () -> UserIdEncrypter.decryptRaw(invalid, key));
  }

  @Test
  @DisplayName("Decrypting a tampered ciphertext should fail with GeneralSecurityException")
  void decrypt_tamperedCipher_shouldFail() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    String userId = "ba035b3e-d8b6-4a09-89c7-ab0459f2585b";

    byte[] enc = UserIdEncrypter.encryptRaw(userId, key);

    // Flip a byte after the IV region so GCM tag validation fails
    int ivLen = CryptoConstants.GCM_IV_LENGTH_BYTES;
    int flipIndex = Math.max(ivLen, Math.min(enc.length - 1, ivLen + 5));
    enc[flipIndex] ^= 0x01;

    assertThrows(
        java.security.GeneralSecurityException.class, () -> UserIdEncrypter.decryptRaw(enc, key));
  }
}
