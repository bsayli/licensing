package io.github.bsayli.license.cli.service;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: KeygenService")
class KeygenServiceTest {

  private final KeygenService svc = new KeygenService();

  @Test
  @DisplayName("AES sizes 128/192/256 should generate decodable Base64")
  void aes_sizes_ok() throws NoSuchAlgorithmException {
    for (int size : new int[] {128, 192, 256}) {
      var out = svc.generateAes(size);
      assertEquals(size, out.sizeBits());
      assertNotNull(out.base64());
      assertFalse(out.base64().isBlank());
      assertDoesNotThrow(() -> B64_DEC.decode(out.base64()));
    }
  }

  @Test
  @DisplayName("Invalid AES size should throw")
  void aes_invalid_size_throws() {
    assertThrows(IllegalArgumentException.class, () -> svc.generateAes(64));
    assertThrows(IllegalArgumentException.class, () -> svc.generateAes(999));
  }

  @Test
  @DisplayName("Ed25519 pair should be decodable to valid keys")
  void ed25519_ok() throws Exception {
    var pair = svc.generateEd25519();
    assertNotNull(pair.publicSpkiB64());
    assertNotNull(pair.privatePkcs8B64());

    byte[] pubDer = B64_DEC.decode(pair.publicSpkiB64());
    byte[] privDer = B64_DEC.decode(pair.privatePkcs8B64());

    var kf = KeyFactory.getInstance("Ed25519");
    var pub = kf.generatePublic(new X509EncodedKeySpec(pubDer));
    var prv = kf.generatePrivate(new PKCS8EncodedKeySpec(privDer));

    // JDK bazı ortamlarda "EdDSA" döndürebiliyor — her ikisini de kabul et.
    assertTrue(java.util.Set.of("Ed25519", "EdDSA").contains(pub.getAlgorithm()));
    assertTrue(java.util.Set.of("Ed25519", "EdDSA").contains(prv.getAlgorithm()));

    assertArrayEquals(pubDer, pub.getEncoded());
    assertArrayEquals(privDer, prv.getEncoded());
  }
}
