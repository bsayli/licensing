package io.github.bsayli.licensing.sdk.generator;

import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;

public interface ClientIdGenerator {

  String getClientId(LicenseAccessRequest request);
}
