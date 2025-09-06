package io.github.bsayli.licensing.sdk.generator;

import io.github.bsayli.licensing.sdk.model.server.LicenseServerValidationRequest;

public interface SignatureGenerator {

  String generateSignature(LicenseServerValidationRequest request);
}
