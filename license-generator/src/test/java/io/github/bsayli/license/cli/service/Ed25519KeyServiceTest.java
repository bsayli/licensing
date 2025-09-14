package io.github.bsayli.license.cli.service;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("unit")
@DisplayName("Unit Test: Ed25519KeyService")
class Ed25519KeyServiceTest {

  private final Ed25519KeyService service = new Ed25519KeyService();

  @Test
  @DisplayName("generate() should return Base64 encodings decodable to valid Ed25519 keys")
  void generate_keys_areValid_andDecodable() throws Exception {
    var keys = service.generate();

    assertNotNull(keys);
    assertNotNull(keys.publicSpkiB64());
    assertNotNull(keys.privatePkcs8B64());
    assertFalse(keys.publicSpkiB64().isBlank());
    assertFalse(keys.privatePkcs8B64().isBlank());

    byte[] pubDer = B64_DEC.decode(keys.publicSpkiB64());
    byte[] privDer = B64_DEC.decode(keys.privatePkcs8B64());

    KeyFactory kf = KeyFactory.getInstance("Ed25519");

    var pub = kf.generatePublic(new X509EncodedKeySpec(pubDer));
    var priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privDer));

    assertTrue(
        java.util.Set.of("Ed25519", "EdDSA").contains(pub.getAlgorithm()),
        "Algorithm should be Ed25519/EdDSA");
    assertTrue(
        java.util.Set.of("Ed25519", "EdDSA").contains(priv.getAlgorithm()),
        "Algorithm should be Ed25519/EdDSA");

    assertArrayEquals(pubDer, pub.getEncoded());
    assertArrayEquals(privDer, priv.getEncoded());
  }

  @Test
  @DisplayName("writeString() should create parent dirs and write exact content")
  void writeString_createsDirs_andWrites(@TempDir Path tmp) throws IOException {
    Path nested = tmp.resolve("keys/out/public.txt");
    String content = "hello-world";

    service.writeString(nested, content);

    assertTrue(Files.exists(nested), "File must exist");
    assertEquals(content, Files.readString(nested));
  }

  @Test
  @DisplayName("ensureBouncyCastleIfNeeded() is idempotent and never throws")
  void ensureBouncyCastle_isIdempotent_andNoThrow() {
    assertDoesNotThrow(service::ensureBouncyCastleIfNeeded);
    assertDoesNotThrow(service::ensureBouncyCastleIfNeeded);
  }

  @Test
  @DisplayName("generate() works with either JDK or BouncyCastle provider")
  void generate_worksWithEitherProvider() {
    assertDoesNotThrow(service::generate);
  }
}
