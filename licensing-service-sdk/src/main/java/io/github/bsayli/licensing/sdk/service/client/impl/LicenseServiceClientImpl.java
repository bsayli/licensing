package io.github.bsayli.licensing.sdk.service.client.impl;

import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.service.client.LicenseServiceClient;
import org.springframework.stereotype.Service;

@Service
public class LicenseServiceClientImpl implements LicenseServiceClient {

  private final LicensingServiceClientAdapter adapter;

  public LicenseServiceClientImpl(LicensingServiceClientAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public ApiClientResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest req) {
    return adapter.issueAccess(req);
  }

  @Override
  public ApiClientResponse<LicenseAccessResponse> validateAccess(
      String token, ValidateAccessRequest req) {
    return adapter.validateAccess(token, req);
  }
}
