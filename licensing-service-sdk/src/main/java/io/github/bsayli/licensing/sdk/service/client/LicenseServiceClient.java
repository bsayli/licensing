package io.github.bsayli.licensing.sdk.service.client;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;

public interface LicenseServiceClient {
  ApiClientResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest req);

  ApiClientResponse<LicenseAccessResponse> validateAccess(String token, ValidateAccessRequest req);
}
