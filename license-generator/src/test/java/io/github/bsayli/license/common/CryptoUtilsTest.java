package io.github.bsayli.license.common;

import static io.github.bsayli.license.common.CryptoUtils.aesGcmDecryptRaw;
import static io.github.bsayli.license.common.CryptoUtils.aesGcmEncryptRaw;
import static io.github.bsayli.license.common.CryptoUtils.concat;
import static io.github.bsayli.license.common.CryptoUtils.loadAesKeyFromBase64;
import static io.github.bsayli.license.common.CryptoUtils.toBase64;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: CryptoUtils (AES/GCM helpers)")
class CryptoUtilsTest {

  private static final String KEY_B64 = "wL6U5l5CwzE0bU3x3Z9o6v7S0X7z4k1kqQx9m8k9c0w=";

  @Test
  @DisplayName("AES-GCM raw round-trip returns original plaintext")
  void aesGcm_roundTrip_ok() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);

    String plain = "hello-license";
    byte[] enc = aesGcmEncryptRaw(key, plain.getBytes(StandardCharsets.UTF_8));
    byte[] dec = aesGcmDecryptRaw(key, enc);

    assertEquals(plain, new String(dec, StandardCharsets.UTF_8));
    assertNotEquals(
        plain, new String(enc, StandardCharsets.UTF_8)); // ciphertext is not readable text
  }

  @Test
  @DisplayName(
      "AES-GCM uses random IV: encrypting same plaintext twice yields different ciphertexts")
  void aesGcm_randomIv_changesCiphertext() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    byte[] p = "same-plaintext".getBytes(StandardCharsets.UTF_8);

    byte[] enc1 = aesGcmEncryptRaw(key, p);
    byte[] enc2 = aesGcmEncryptRaw(key, p);

    assertFalse(Arrays.equals(enc1, enc2), "Ciphertexts should differ due to random IV");
    assertTrue(enc1.length > 0 && enc2.length > 0);
  }

  @Test
  @DisplayName("AES-GCM decryption fails if ciphertext is tampered")
  void aesGcm_decrypt_throwsOnTamper() throws Exception {
    SecretKey key = loadAesKeyFromBase64(KEY_B64);
    byte[] enc = aesGcmEncryptRaw(key, "payload".getBytes(StandardCharsets.UTF_8));

    // Flip a byte past the IV region to ensure tag validation fails.
    int ivLen = CryptoConstants.GCM_IV_LENGTH_BYTES; // 12
    int flipIndex = Math.max(ivLen, Math.min(enc.length - 1, ivLen + 5));
    enc[flipIndex] ^= 0x01;

    assertThrows(Exception.class, () -> aesGcmDecryptRaw(key, enc));
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
