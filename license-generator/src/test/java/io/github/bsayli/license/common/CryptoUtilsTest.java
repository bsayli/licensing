package io.github.bsayli.license.common;

import static io.github.bsayli.license.common.CryptoUtils.aesGcmDecryptFromBase64;
import static io.github.bsayli.license.common.CryptoUtils.aesGcmEncryptToBase64;
import static io.github.bsayli.license.common.CryptoUtils.concat;
import static io.github.bsayli.license.common.CryptoUtils.loadAesKeyFromBase64;
import static io.github.bsayli.license.common.CryptoUtils.toBase64;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: CryptoUtils (AES/GCM helpers)")
class CryptoUtilsTest {

  // 256-bit AES key (Base64). Test-only fixture.
  private static final String KEY_B64 = "wL6U5l5CwzE0bU3x3Z9o6v7S0X7z4k1kqQx9m8k9c0w=";

  @Test
  @DisplayName("AES-GCM round-trip returns original plaintext")
  void aesGcm_roundTrip_ok() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);

    String plain = "hello-license";
    String enc = aesGcmEncryptToBase64(key, plain);
    String dec = aesGcmDecryptFromBase64(key, enc);

    assertEquals(plain, dec);
    assertNotEquals(plain, enc);
  }

  @Test
  @DisplayName(
      "AES-GCM uses random IV: encrypting same plaintext twice yields different ciphertexts")
  void aesGcm_randomIv_changesCiphertext() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    String plain = "same-plaintext";

    String enc1 = aesGcmEncryptToBase64(key, plain);
    String enc2 = aesGcmEncryptToBase64(key, plain);

    assertNotEquals(enc1, enc2, "Ciphertexts should differ due to random IV");
    // sanity: both look like Base64
    assertDoesNotThrow(() -> Base64.getDecoder().decode(enc1));
    assertDoesNotThrow(() -> Base64.getDecoder().decode(enc2));
  }

  @Test
  @DisplayName("AES-GCM decryption fails if ciphertext is tampered")
  void aesGcm_decrypt_throwsOnTamper() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    String enc = aesGcmEncryptToBase64(key, "payload");

    // flip 1 byte somewhere in the middle (post-IV region most likely)
    byte[] bytes = java.util.Base64.getDecoder().decode(enc);
    int flipIndex = Math.min(bytes.length - 1, 20);
    bytes[flipIndex] ^= 0x01;

    String tampered = java.util.Base64.getEncoder().encodeToString(bytes);
    assertThrows(Exception.class, () -> aesGcmDecryptFromBase64(key, tampered));
  }

  @Test
  @DisplayName("toBase64(load(base64)) yields the original Base64 key")
  void loadKey_and_toBase64_roundTrip_ok() {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    String again = toBase64(key);
    assertEquals(KEY_B64, again);
  }

  @Test
  @DisplayName("concat(a,b) joins arrays in order")
  void concat_joinsArrays() {
    byte[] a = {1, 2, 3};
    byte[] b = {4, 5};
    byte[] out = concat(a, b);

    assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, out);
  }
}
