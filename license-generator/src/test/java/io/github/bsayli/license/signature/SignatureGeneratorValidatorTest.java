package io.github.bsayli.license.signature;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SignatureGenerator + SignatureValidator (Ed25519)")
class SignatureGeneratorValidatorTest {

  private static KeyPair generateEd25519() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
    return kpg.generateKeyPair();
  }

  private static String b64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  @Test
  @DisplayName("Signature over encryptedLicenseKeyHash should verify")
  void signature_overEncryptedKeyHash_shouldVerify() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded()); // PKCS#8
    String publicKeyB64 = b64(kp.getPublic().getEncoded()); // X.509 (SPKI)

    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);
    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    boolean ok = validator.validateSignature(signatureB64, json);

    assertTrue(ok, "Signature must verify for original JSON");
  }

  @Test
  @DisplayName("Signature over licenseTokenHash should verify")
  void signature_overTokenHash_shouldVerify() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded()); // PKCS#8
    String publicKeyB64 = b64(kp.getPublic().getEncoded()); // SPKI

    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseToken();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);
    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    boolean ok = validator.validateSignature(signatureB64, json);

    assertTrue(ok, "Signature must verify for original JSON");
  }

  @Test
  @DisplayName("Tampered JSON should not verify")
  void tampered_json_shouldNotVerify() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded());
    String publicKeyB64 = b64(kp.getPublic().getEncoded());

    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();
    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertTrue(validator.validateSignature(signatureB64, json));
    String tampered = json.replace("\"serviceVersion\":\"1.2.2\"", "\"serviceVersion\":\"9.9.9\"");

    boolean okTampered = validator.validateSignature(signatureB64, tampered);
    assertFalse(okTampered, "Signature must fail on tampered JSON");
  }
}
