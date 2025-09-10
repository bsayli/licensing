package io.github.bsayli.licensing.client.adapter;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueTokenRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateTokenRequest;

public interface LicensingServiceClientAdapter {

  ApiClientResponse<LicenseValidationResponse> issueToken(IssueTokenRequest request);

  ApiClientResponse<LicenseValidationResponse> validateToken(
      String licenseToken, ValidateTokenRequest request);
}
