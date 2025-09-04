package io.github.bsayli.license.signature.validator;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureValidator")
class SignatureValidatorTest {

  @Test
  @DisplayName("validateSignature returns true for a matching signature & JSON")
  void validate_true_onValidSignature() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String json = payload.toJson();
    String sig = SignatureGenerator.createSignature(payload);

    assertTrue(new SignatureValidator().validateSignature(sig, json));
  }

  @Test
  @DisplayName("validateSignature returns false when JSON is different")
  void validate_false_onDifferentJson() throws Exception {
    SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
    String sig = SignatureGenerator.createSignature(payload);

    String differentJson = payload.toJson() + " ";
    assertFalse(new SignatureValidator().validateSignature(sig, differentJson));
  }
}
