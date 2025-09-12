package io.github.bsayli.licensing.sdk.service.impl;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.common.exception.ApiClientException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseToken;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkHttpTransportException;
import io.github.bsayli.licensing.sdk.generator.ClientIdGenerator;
import io.github.bsayli.licensing.sdk.generator.SignatureGenerator;
import io.github.bsayli.licensing.sdk.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.sdk.service.LicenseTokenCacheService;
import io.github.bsayli.licensing.sdk.service.client.LicenseServiceClient;
import io.github.bsayli.licensing.sdk.service.handler.LicenseResponseHandler;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

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
    this.licenseServiceClient = licenseServiceClient;
    this.licenseTokenCacheService = licenseTokenCacheService;
    this.clientIdGenerator = clientIdGenerator;
    this.signatureGenerator = signatureGenerator;
    this.responseHandler = responseHandler;
  }

  @Override
  public LicenseToken getLicenseToken(LicenseAccessRequest request) {
    final String clientId = clientIdGenerator.getClientId(request);
    final String cached = licenseTokenCacheService.get(clientId);

    try {
      if (cached == null) {
        IssueAccessRequest issueReq =
            new IssueAccessRequest()
                .serviceId(request.serviceId())
                .serviceVersion(request.serviceVersion())
                .instanceId(request.instanceId())
                .checksum(request.checksum())
                .licenseKey(request.licenseKey());

        issueReq.setSignature(signatureGenerator.generateForIssue(issueReq));

        ApiClientResponse<LicenseAccessResponse> resp = licenseServiceClient.issueAccess(issueReq);
        String token = responseHandler.extractTokenOrThrow(resp);

        licenseTokenCacheService.put(clientId, token);
        return new LicenseToken(token);
      }

      ValidateAccessRequest validateReq =
          new ValidateAccessRequest()
              .serviceId(request.serviceId())
              .serviceVersion(request.serviceVersion())
              .instanceId(request.instanceId())
              .checksum(request.checksum());

      validateReq.setSignature(signatureGenerator.generateForValidate(cached, validateReq));

      ApiClientResponse<LicenseAccessResponse> vResp =
          licenseServiceClient.validateAccess(cached, validateReq);

      String maybeRefreshed = responseHandler.tokenIfOkOrNullOrThrow(vResp);
      if (maybeRefreshed != null && !maybeRefreshed.isBlank()) {
        licenseTokenCacheService.put(clientId, maybeRefreshed);
        return new LicenseToken(maybeRefreshed);
      }

      return new LicenseToken(cached);

    } catch (ApiClientException e) {
      HttpStatusCode status = e.getStatusCode();
      String msgKey = e.getMessageKey();
      String raw = e.getResponseBody();
      throw new LicensingSdkHttpTransportException(
          "[SDK] transport/parse error when calling licensing-service", status, msgKey, raw, e);
    }
  }
}
