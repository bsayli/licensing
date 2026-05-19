package io.github.bsayli.licensing.sdk.service.client;

import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.contract.api.ApiResponse;

public interface LicenseServiceClient {
  ApiResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest req);

  ApiResponse<LicenseAccessResponse> validateAccess(String token, ValidateAccessRequest req);
}
