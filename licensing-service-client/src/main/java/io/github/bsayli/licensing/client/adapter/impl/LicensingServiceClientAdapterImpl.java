package io.github.bsayli.licensing.client.adapter.impl;

import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.common.core.ApiClientExecutor;
import io.github.bsayli.licensing.client.generated.api.LicenseControllerApi;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.springframework.stereotype.Service;

@Service
public class LicensingServiceClientAdapterImpl implements LicensingServiceClientAdapter {

  private final LicenseControllerApi api;
  private final ApiClientExecutor executor;

  public LicensingServiceClientAdapterImpl(LicenseControllerApi api, ApiClientExecutor executor) {
    this.api = api;
    this.executor = executor;
  }

  @Override
  public ApiClientResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest request) {
    return executor.handle(
        "issueAccess", LicenseAccessResponse.class, () -> api.createAccess(request));
  }

  @Override
  public ApiClientResponse<LicenseAccessResponse> validateAccess(
      String licenseToken, ValidateAccessRequest request) {
    return executor.handle(
        "validateAccess",
        LicenseAccessResponse.class,
        () -> api.validateAccess(licenseToken, request));
  }
}
