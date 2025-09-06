package io.github.bsayli.licensing.sdk.generator;

import io.github.bsayli.licensing.sdk.model.LicenseValidationRequest;

public interface ClientIdGenerator {

  String getClientId(LicenseValidationRequest request);
}
