package io.github.bsayli.licensing.agent.service.impl;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.agent.generator.ClientIdGenerator;
import io.github.bsayli.licensing.agent.generator.SignatureGenerator;
import io.github.bsayli.licensing.agent.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.agent.service.LicenseTokenCacheService;
import io.github.bsayli.licensing.agent.service.client.LicenseServiceClient;
import io.github.bsayli.licensing.agent.service.handler.LicenseResponseHandler;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

    private static final String CODE_TOKEN_TOO_OLD = "TOKEN_IS_TOO_OLD_FOR_REFRESH";

    private final LicenseServiceClient licenseServiceClient;
    private final LicenseTokenCacheService licenseTokenCacheService;
    private final ClientIdGenerator clientIdGenerator;
    private final SignatureGenerator signatureGenerator;
    private final LicenseResponseHandler responseHandler;

    public LicenseOrchestrationServiceImpl(
            LicenseServiceClient licenseServiceClient,
            LicenseTokenCacheService licenseTokenCacheService,
            ClientIdGenerator clientIdGenerator,
            SignatureGenerator signatureGenerator,
            LicenseResponseHandler responseHandler) {
        this.licenseServiceClient = Objects.requireNonNull(licenseServiceClient);
        this.licenseTokenCacheService = Objects.requireNonNull(licenseTokenCacheService);
        this.clientIdGenerator = Objects.requireNonNull(clientIdGenerator);
        this.signatureGenerator = Objects.requireNonNull(signatureGenerator);
        this.responseHandler = Objects.requireNonNull(responseHandler);
    }

    @Override
    public LicenseToken getLicenseToken(LicenseAccessRequest request) {
        final String clientId = clientIdGenerator.getClientId(request);
        final String cached = licenseTokenCacheService.get(clientId);

        try {
            if (cached == null) {
                String token = issueAndCacheToken(request, clientId);
                return new LicenseToken(token);
            }

            String token = validateThenMaybeRefreshOrReissue(request, clientId, cached);
            return new LicenseToken(token);

        } catch (ApiProblemException e) {
            throw responseHandler.mapRemoteFailure(e);
        }
    }

    private String validateThenMaybeRefreshOrReissue(
            LicenseAccessRequest request, String clientId, String cachedToken) {

        ValidateAccessRequest validateReq =
                new ValidateAccessRequest()
                        .serviceId(request.serviceId())
                        .serviceVersion(request.serviceVersion())
                        .instanceId(request.instanceId())
                        .checksum(request.checksum());

        validateReq.setSignature(signatureGenerator.generateForValidate(cachedToken, validateReq));

        try {
            ServiceResponse<LicenseAccessResponse> vResp =
                    licenseServiceClient.validateAccess(cachedToken, validateReq);

            String maybeRefreshed = responseHandler.extractTokenIfPresent(vResp);
            if (maybeRefreshed != null && !maybeRefreshed.isBlank()) {
                licenseTokenCacheService.put(clientId, maybeRefreshed);
                return maybeRefreshed;
            }

            return cachedToken;

        } catch (ApiProblemException e) {
            LicensingSdkRemoteServiceException remote = responseHandler.mapRemoteFailure(e);
            if (CODE_TOKEN_TOO_OLD.equals(remote.getErrorCode())) {
                return issueAndCacheToken(request, clientId);
            }
            throw remote;
        }
    }

    private String issueAndCacheToken(LicenseAccessRequest request, String clientId) {
        IssueAccessRequest issueReq =
                new IssueAccessRequest()
                        .serviceId(request.serviceId())
                        .serviceVersion(request.serviceVersion())
                        .instanceId(request.instanceId())
                        .checksum(request.checksum())
                        .licenseKey(request.licenseKey());

        issueReq.setSignature(signatureGenerator.generateForIssue(issueReq));

        try {
            ServiceResponse<LicenseAccessResponse> resp = licenseServiceClient.issueAccess(issueReq);
            String token = responseHandler.extractTokenOrThrow(resp);

            licenseTokenCacheService.put(clientId, token);
            return token;

        } catch (ApiProblemException e) {
            throw responseHandler.mapRemoteFailure(e);
        }
    }
}