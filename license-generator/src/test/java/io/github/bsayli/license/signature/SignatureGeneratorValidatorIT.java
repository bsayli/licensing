package io.github.bsayli.license.signature;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureGenerator + SignatureValidator")
class SignatureGeneratorValidatorIT {

  @Test
  @DisplayName("Signature over encryptedLicenseKeyHash should verify")
  void signature_overEncryptedKeyHash_shouldVerify() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload);

    SignatureValidator validator = new SignatureValidator();
    boolean ok = validator.validateSignature(signatureB64, json);

    assertTrue(ok);
  }

  @Test
  @DisplayName("Signature over licenseTokenHash should verify")
  void signature_overTokenHash_shouldVerify() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseToken();
    String json = payload.toJson();

    String signatureB64 = SignatureGenerator.createSignature(payload);

    SignatureValidator validator = new SignatureValidator();
    boolean ok = validator.validateSignature(signatureB64, json);

    assertTrue(ok);
  }

  @Test
  @DisplayName("Tampered JSON should not verify")
  void tampered_json_shouldNotVerify() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();
    String signatureB64 = SignatureGenerator.createSignature(payload);

    String tampered = json.replace("\"serviceVersion\":\"1.2.2\"", "\"serviceVersion\":\"9.9.9\"");

    SignatureValidator validator = new SignatureValidator();
    boolean okOriginal = validator.validateSignature(signatureB64, json);
    boolean okTampered = validator.validateSignature(signatureB64, tampered);

    assertTrue(okOriginal);
    assertFalse(okTampered);
  }
}
