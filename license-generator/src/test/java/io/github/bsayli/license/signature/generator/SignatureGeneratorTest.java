package io.github.bsayli.license.signature.generator;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureGenerator (Ed25519)")
class SignatureGeneratorTest {

  private static KeyPair generateEd25519() throws Exception {
    return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  private static String b64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  private static SignatureData payloadWithEncKeyHash() throws Exception {
    String hashB64 = SignatureGenerator.base64Sha256("dummy-encrypted-license-key");
    return new SignatureData.Builder()
        .serviceId("bsayli-licensing")
        .serviceVersion("1.0.0")
        .instanceId("bsayli-licensing~localdev~mac")
        .encryptedLicenseKeyHash(hashB64)
        .build();
  }

  private static SignatureData payloadWithTokenHash() throws Exception {
    String hashB64 = SignatureGenerator.base64Sha256("dummy-license-token");
    return new SignatureData.Builder()
        .serviceId("bsayli-licensing")
        .serviceVersion("1.0.0")
        .instanceId("bsayli-licensing~localdev~mac")
        .licenseTokenHash(hashB64)
        .build();
  }

  @Test
  @DisplayName("Signing payload (encryptedLicenseKeyHash) then verifying should succeed")
  void sign_verify_withEncryptedKeyHash_ok() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded()); // PKCS#8
    String publicKeyB64 = b64(kp.getPublic().getEncoded()); // SPKI

    SignatureData payload = payloadWithEncKeyHash();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertTrue(validator.validateSignature(signatureB64, json));
  }

  @Test
  @DisplayName("Signing payload (licenseTokenHash) then verifying should succeed")
  void sign_verify_withLicenseTokenHash_ok() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded());
    String publicKeyB64 = b64(kp.getPublic().getEncoded());

    SignatureData payload = payloadWithTokenHash();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertTrue(validator.validateSignature(signatureB64, json));
  }

  @Test
  @DisplayName("Altering JSON after signing should fail verification")
  void tampered_json_shouldFailVerification() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded());
    String publicKeyB64 = b64(kp.getPublic().getEncoded());

    SignatureData payload = payloadWithEncKeyHash();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);
    String tamperedJson = json + " ";

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertFalse(validator.validateSignature(signatureB64, tamperedJson));
  }

  @Test
  @DisplayName("Altering signature bytes should fail verification")
  void tampered_signature_shouldFailVerification() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded());
    String publicKeyB64 = b64(kp.getPublic().getEncoded());

    SignatureData payload = payloadWithEncKeyHash();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);

    byte[] sig = Base64.getDecoder().decode(signatureB64);
    sig[sig.length - 1] ^= 0x01; // flip last bit
    String tamperedSigB64 = Base64.getEncoder().encodeToString(sig);

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertFalse(validator.validateSignature(tamperedSigB64, json));
  }
}
