package io.github.bsayli.licensing.generator;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.ClientInfo;

public interface ClientIdGenerator {

  String getClientId(LicenseValidationRequest request);

  String getClientId(ClientInfo clientInfo);
}
