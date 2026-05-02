package io.github.bsayli.licensing.agent.service.client;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;

public interface LicenseServiceClient {
    ServiceResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest req);

    ServiceResponse<LicenseAccessResponse> validateAccess(String token, ValidateAccessRequest req);
}
