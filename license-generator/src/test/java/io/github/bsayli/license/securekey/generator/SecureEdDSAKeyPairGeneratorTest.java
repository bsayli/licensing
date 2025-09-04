package io.github.bsayli.license.securekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;
import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.CryptoUtils;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SecureEdDSAKeyPairGenerator")
class SecureEdDSAKeyPairGeneratorTest {

  @Test
  @DisplayName("generateKeyPair should produce valid Ed25519 keys")
  void generateKeyPair_ok() throws GeneralSecurityException {
    KeyPair kp1 = SecureEdDSAKeyPairGenerator.generateKeyPair();
    KeyPair kp2 = SecureEdDSAKeyPairGenerator.generateKeyPair();

    assertNotNull(kp1.getPublic());
    assertNotNull(kp1.getPrivate());

    // algorithm name check (either Ed25519 or EdDSA depending on provider)
    assertTrue(
        kp1.getPublic().getAlgorithm().equalsIgnoreCase("Ed25519")
            || kp1.getPublic().getAlgorithm().equalsIgnoreCase("EdDSA"));

    // consecutive keypairs should differ
    assertNotEquals(
        Base64.getEncoder().encodeToString(kp1.getPublic().getEncoded()),
        Base64.getEncoder().encodeToString(kp2.getPublic().getEncoded()));
  }

  @Test
  @DisplayName("Generated keys should successfully sign and verify")
  void signAndVerify_ok() throws Exception {
    KeyPair kp = SecureEdDSAKeyPairGenerator.generateKeyPair();
    byte[] message = "hello-eddsa".getBytes();

    Signature sig = Signature.getInstance(ED25519_STD_ALGO);
    sig.initSign(kp.getPrivate());
    sig.update(message);
    byte[] signature = sig.sign();

    Signature verifier = Signature.getInstance(ED25519_STD_ALGO);
    verifier.initVerify(kp.getPublic());
    verifier.update(message);

    assertTrue(verifier.verify(signature));
  }

  @Test
  @DisplayName("CryptoUtils.toBase64 should serialize public/private keys")
  void base64Serialization_ok() throws Exception {
    KeyPair kp = SecureEdDSAKeyPairGenerator.generateKeyPair();

    String pubB64 = CryptoUtils.toBase64(kp.getPublic());
    String privB64 = CryptoUtils.toBase64(kp.getPrivate());

    assertNotNull(pubB64);
    assertNotNull(privB64);

    assertArrayEquals(kp.getPublic().getEncoded(), Base64.getDecoder().decode(pubB64));
    assertArrayEquals(kp.getPrivate().getEncoded(), Base64.getDecoder().decode(privB64));
  }
}
