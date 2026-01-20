package io.github.bsayli.licensing.agent.service.client.impl;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.service.client.LicenseServiceClient;
import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.springframework.stereotype.Service;

@Service
public class LicenseServiceClientImpl implements LicenseServiceClient {

    private final LicensingServiceClientAdapter adapter;

    public LicenseServiceClientImpl(LicensingServiceClientAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public ServiceResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest req) {
        return adapter.issueAccess(req);
    }

    @Override
    public ServiceResponse<LicenseAccessResponse> validateAccess(
            String token, ValidateAccessRequest req) {
        return adapter.validateAccess(token, req);
    }
}
