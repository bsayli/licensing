package com.c9.licensing.sdk.generator;

import com.c9.licensing.sdk.model.server.LicenseServerValidationRequest;

public interface SignatureGenerator {

  String generateSignature(LicenseServerValidationRequest request);
}
