package io.github.bsayli.licensing.client.adapter.impl;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.generated.api.LicenseControllerApi;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.springframework.stereotype.Service;

@Service
public class LicensingServiceClientAdapterImpl implements LicensingServiceClientAdapter {

    private final LicenseControllerApi api;

    public LicensingServiceClientAdapterImpl(LicenseControllerApi api) {
        this.api = api;
    }

    @Override
    public ServiceResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest request) {
        return api.createAccess(request);
    }

    @Override
    public ServiceResponse<LicenseAccessResponse> validateAccess(
            String licenseToken, ValidateAccessRequest request) {
        return api.validateAccess(licenseToken, request);
    }
}