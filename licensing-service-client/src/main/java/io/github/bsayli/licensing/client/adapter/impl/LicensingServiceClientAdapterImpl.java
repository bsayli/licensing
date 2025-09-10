package io.github.bsayli.licensing.client.adapter.impl;

import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.common.core.ApiClientExecutor;
import io.github.bsayli.licensing.client.generated.api.LicenseValidationControllerApi;
import io.github.bsayli.licensing.client.generated.dto.IssueTokenRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateTokenRequest;
import org.springframework.stereotype.Service;

@Service
public class LicensingServiceClientAdapterImpl implements LicensingServiceClientAdapter {

  private final LicenseValidationControllerApi api;
  private final ApiClientExecutor executor;

  public LicensingServiceClientAdapterImpl(
      LicenseValidationControllerApi api, ApiClientExecutor executor) {
    this.api = api;
    this.executor = executor;
  }

  @Override
  public ApiClientResponse<LicenseValidationResponse> issueToken(IssueTokenRequest request) {
    return executor.handle(
        "issueToken", LicenseValidationResponse.class, () -> api.issueToken(request));
  }

  @Override
  public ApiClientResponse<LicenseValidationResponse> validateToken(
      String licenseToken, ValidateTokenRequest request) {
    return executor.handle(
        "validateToken",
        LicenseValidationResponse.class,
        () -> api.validateToken(licenseToken, request));
  }
}
