package io.github.bsayli.licensing.agent.generator;

import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;

public interface ClientIdGenerator {

    String getClientId(LicenseAccessRequest request);
}
