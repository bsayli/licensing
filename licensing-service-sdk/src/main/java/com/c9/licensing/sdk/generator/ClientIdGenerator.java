package com.c9.licensing.sdk.generator;

import com.c9.licensing.sdk.model.LicenseValidationRequest;

public interface ClientIdGenerator {

  String getClientId(LicenseValidationRequest request);
}
