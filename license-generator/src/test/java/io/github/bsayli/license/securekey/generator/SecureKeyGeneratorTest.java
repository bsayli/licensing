package io.github.bsayli.license.securekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.AES_KEY_ALGORITHM;
import static io.github.bsayli.license.common.CryptoConstants.DEFAULT_AES_KEY_BITS;
import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.CryptoUtils;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SecureKeyGenerator")
class SecureKeyGeneratorTest {

  @Test
  @DisplayName("generateAesKey(256) should produce a 256-bit AES key")
  void generateAes256_ok() throws Exception {
    SecretKey k1 = SecureKeyGenerator.generateAesKey(DEFAULT_AES_KEY_BITS);
    SecretKey k2 = SecureKeyGenerator.generateAesKey(DEFAULT_AES_KEY_BITS);

    assertNotNull(k1);
    assertEquals(AES_KEY_ALGORITHM, k1.getAlgorithm());
    assertEquals(32, k1.getEncoded().length, "256-bit key must be 32 bytes");

    // randomness: two consecutive keys should differ
    assertNotEquals(
        Base64.getEncoder().encodeToString(k1.getEncoded()),
        Base64.getEncoder().encodeToString(k2.getEncoded()));
  }

  @Test
  @DisplayName("generateAesKey(128) should produce a 128-bit AES key")
  void generateAes128_ok() throws Exception {
    SecretKey k = SecureKeyGenerator.generateAesKey(128);

    assertNotNull(k);
    assertEquals(AES_KEY_ALGORITHM, k.getAlgorithm());
    assertEquals(16, k.getEncoded().length, "128-bit key must be 16 bytes");
  }

  @Test
  @DisplayName("toBase64 should round-trip to the same key bytes")
  void base64RoundTrip_ok() throws Exception {
    SecretKey k = SecureKeyGenerator.generateAesKey(DEFAULT_AES_KEY_BITS);

    String b64 = CryptoUtils.toBase64(k);
    byte[] decoded = Base64.getDecoder().decode(b64);

    assertArrayEquals(k.getEncoded(), decoded);
  }
}
