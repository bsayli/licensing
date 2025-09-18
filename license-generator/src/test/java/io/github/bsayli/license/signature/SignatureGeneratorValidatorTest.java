package io.github.bsayli.license.signature;

import static io.github.bsayli.license.common.CryptoConstants.B64_ENC;
import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.common.CryptoUtils;
import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SignatureGenerator + SignatureValidator (Ed25519)")
class SignatureGeneratorValidatorTest {

  private static KeyPair generateEd25519() throws Exception {
    return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  private static String b64(byte[] bytes) {
    return B64_ENC.encodeToString(bytes);
  }

  private static SignatureData buildPayloadWithEncKeyHash(String encKeyPlain) throws Exception {
    String encKeyHashB64 = CryptoUtils.base64Sha256(encKeyPlain);
    return new SignatureData.Builder()
        .serviceId("bsayli-licensing")
        .serviceVersion("1.0.0")
        .instanceId("bsayli-licensing~localdev~mac")
        .encryptedLicenseKeyHash(encKeyHashB64)
        .build();
  }

  private static SignatureData buildPayloadWithTokenHash(String tokenPlain) throws Exception {
    String tokenHashB64 = CryptoUtils.base64Sha256(tokenPlain);
    return new SignatureData.Builder()
        .serviceId("bsayli-licensing")
        .serviceVersion("1.0.0")
        .instanceId("bsayli-licensing~localdev~mac")
        .licenseTokenHash(tokenHashB64)
        .build();
  }

  @Test
  @DisplayName("Signature over encryptedLicenseKeyHash should verify")
  void signature_overEncryptedKeyHash_shouldVerify() throws Exception {
    KeyPair kp = generateEd25519();
    String privateKeyB64 = b64(kp.getPrivate().getEncoded()); // PKCS#8
    String publicKeyB64 = b64(kp.getPublic().getEncoded()); // X.509 (SPKI)

    SignatureData payload = buildPayloadWithEncKeyHash("dummy-encrypted-license-key");
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
    String privateKeyB64 = b64(kp.getPrivate().getEncoded());
    String publicKeyB64 = b64(kp.getPublic().getEncoded());

    SignatureData payload = buildPayloadWithTokenHash("dummy-license-token");
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

    SignatureData payload = buildPayloadWithEncKeyHash("another-dummy-enc-key");
    String json = payload.toJson();
    String signatureB64 = SignatureGenerator.createSignature(payload, privateKeyB64);

    SignatureValidator validator = new SignatureValidator(publicKeyB64);
    assertTrue(validator.validateSignature(signatureB64, json));

    // mutate one field -> signature must fail
    String tampered = json.replace("\"serviceVersion\":\"1.0.0\"", "\"serviceVersion\":\"9.9.9\"");

    boolean okTampered = validator.validateSignature(signatureB64, tampered);
    assertFalse(okTampered, "Signature must fail on tampered JSON");
  }
}
