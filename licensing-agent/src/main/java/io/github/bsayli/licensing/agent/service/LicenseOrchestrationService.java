package io.github.bsayli.licensing.agent.service;

import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;

public interface LicenseOrchestrationService {

    LicenseToken getLicenseToken(LicenseAccessRequest request);
}
