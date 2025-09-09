package io.github.bsayli.license.signature.validator;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureValidator with Ed25519")
class SignatureValidatorTest {

  private static KeyPair generateEd25519() throws Exception {
    return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  private static String b64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  @Test
  @DisplayName("validateSignature returns true for a matching signature & JSON")
  void validate_true_onValidSignature() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded()); // PKCS#8
    String publicKeyB64 = b64(kp.getPublic().getEncoded()); // SPKI

    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();
    String sig = SignatureGenerator.createSignature(payload, privateKeyB64);

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertTrue(validator.validateSignature(sig, json));
  }

  @Test
  @DisplayName("validateSignature returns false when JSON is different")
  void validate_false_onDifferentJson() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded());
    String publicKeyB64 = b64(kp.getPublic().getEncoded());

    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();
    String sig = SignatureGenerator.createSignature(payload, privateKeyB64);

    String differentJson = json + " ";

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertFalse(validator.validateSignature(sig, differentJson));
  }
}
