package io.github.bsayli.licensing.client.adapter;

import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.contract.api.ApiResponse;

public interface LicensingServiceClientAdapter {

  ApiResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest request);

  ApiResponse<LicenseAccessResponse> validateAccess(
      String licenseToken, ValidateAccessRequest request);
}
