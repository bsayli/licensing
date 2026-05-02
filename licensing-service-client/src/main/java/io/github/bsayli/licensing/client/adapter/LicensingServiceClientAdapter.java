package io.github.bsayli.licensing.client.adapter;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;

public interface LicensingServiceClientAdapter {

    ServiceResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest request);

    ServiceResponse<LicenseAccessResponse> validateAccess(
            String licenseToken, ValidateAccessRequest request);
}
