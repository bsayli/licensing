package io.github.bsayli.license.signature.generator;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureGenerator")
class SignatureGeneratorTest {

  @Test
  @DisplayName("Signing payload (encryptedLicenseKeyHash) then verifying should succeed")
  void sign_verify_withEncryptedKeyHash_ok() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload);

    SignatureValidator validator = new SignatureValidator();
    assertTrue(validator.validateSignature(signatureB64, json));
  }

  @Test
  @DisplayName("Signing payload (licenseTokenHash) then verifying should succeed")
  void sign_verify_withLicenseTokenHash_ok() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseToken();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload);

    SignatureValidator validator = new SignatureValidator();
    assertTrue(validator.validateSignature(signatureB64, json));
  }

  @Test
  @DisplayName("Altering JSON after signing should fail verification")
  void tampered_json_shouldFailVerification() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload);
    String tamperedJson = json + " ";

    SignatureValidator validator = new SignatureValidator();
    assertFalse(validator.validateSignature(signatureB64, tamperedJson));
  }

  @Test
  @DisplayName("Altering signature bytes should fail verification")
  void tampered_signature_shouldFailVerification() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload);

    byte[] sig = java.util.Base64.getDecoder().decode(signatureB64);
    sig[sig.length - 1] ^= 0x01; // son baytta bit çevir
    String tamperedSigB64 = java.util.Base64.getEncoder().encodeToString(sig);

    SignatureValidator validator = new SignatureValidator();
    assertFalse(validator.validateSignature(tamperedSigB64, json));
  }
}
