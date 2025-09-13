package io.github.bsayli.licensing.client.adapter;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;

public interface LicensingServiceClientAdapter {

  ApiClientResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest request);

  ApiClientResponse<LicenseAccessResponse> validateAccess(
      String licenseToken, ValidateAccessRequest request);
}
