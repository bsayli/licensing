package io.github.bsayli.licensing.client.adapter.impl;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.common.core.ApiClientExecutor;
import io.github.bsayli.licensing.client.generated.api.LicenseControllerApi;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.springframework.stereotype.Service;

@Service
public class LicensingServiceClientAdapterImpl implements LicensingServiceClientAdapter {

    private final LicenseControllerApi api;
    private final ApiClientExecutor executor;

    public LicensingServiceClientAdapterImpl(
            LicenseControllerApi api, ApiClientExecutor executor) {
        this.api = api;
        this.executor = executor;
    }

    @Override
    public ServiceResponse<LicenseAccessResponse> issueAccess(IssueAccessRequest request) {
        return executor.handle("issueAccess", () -> api.createAccess(request));
    }

    @Override
    public ServiceResponse<LicenseAccessResponse> validateAccess(
            String licenseToken, ValidateAccessRequest request) {
        return executor.handle(
                "validateAccess", () -> api.validateAccess(licenseToken, request));
    }
}