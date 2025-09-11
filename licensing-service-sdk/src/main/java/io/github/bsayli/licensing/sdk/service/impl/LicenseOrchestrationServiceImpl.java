package io.github.bsayli.licensing.sdk.service.impl;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseToken;
import io.github.bsayli.licensing.sdk.generator.ClientIdGenerator;
import io.github.bsayli.licensing.sdk.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.sdk.service.LicenseTokenService;
import io.github.bsayli.licensing.sdk.service.client.LicenseServiceClient;
import org.springframework.stereotype.Service;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

  private final LicenseServiceClient licenseServiceClient;
  private final LicenseTokenService licenseTokenService;
  private final ClientIdGenerator clientIdGenerator;

  public LicenseOrchestrationServiceImpl(
      LicenseServiceClient licenseServiceClient,
      LicenseTokenService licenseTokenService,
      ClientIdGenerator clientIdGenerator) {
    this.licenseServiceClient = licenseServiceClient;
    this.licenseTokenService = licenseTokenService;
    this.clientIdGenerator = clientIdGenerator;
  }

  @Override
  public LicenseToken getLicenseToken(LicenseAccessRequest request) {
    final String clientId = clientIdGenerator.getClientId(request);
    final String cached = licenseTokenService.getLicenseToken(clientId);

    if (cached == null) {
      IssueAccessRequest issueReq =
          new IssueAccessRequest()
              .serviceId(request.serviceId())
              .serviceVersion(request.serviceVersion())
              .instanceId(request.instanceId())
              // imza üretimini sonraki adımda ekleyebiliriz:
              .signature("PLACEHOLDER_SIG")
              .checksum(request.checksum())
              .licenseKey(request.licenseKey())
              .forceTokenRefresh(Boolean.FALSE);

      ApiClientResponse<LicenseAccessResponse> resp = licenseServiceClient.issueAccess(issueReq);

      // basit kontrol: 200 ve data/token bekliyoruz
      if (resp.getStatus() != null && resp.getStatus() == 200 && resp.getData() != null) {
        String newToken = resp.getData().getLicenseToken();
        if (newToken == null || newToken.isBlank()) {
          throw new IllegalStateException("Licensing-service returned success but token is empty.");
        }
        licenseTokenService.storeLicenseToken(clientId, newToken);
        return new LicenseToken(newToken);
      }

      // hata durumunu şimdilik sade şekilde yüzeye taşıyalım
      throw new IllegalStateException("Issue access failed: " + resp.getMessage());
    }

    // 4) token var => VALIDATE akışı
    ValidateAccessRequest validateReq =
        new ValidateAccessRequest()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            // imza üretimini sonraki adımda ekleyebiliriz:
            .signature("PLACEHOLDER_SIG")
            .checksum(request.checksum());

    ApiClientResponse<LicenseAccessResponse> vResp =
        licenseServiceClient.validateAccess(cached, validateReq);

    // 5) validate başarılı ise:
    if (vResp.getStatus() != null && vResp.getStatus() == 200 && vResp.getData() != null) {
      // TOKEN_ACTIVE ise genelde body’de token dönmez; cache’teki aynen kullanılır.
      String maybeRefreshed = vResp.getData().getLicenseToken();
      if (maybeRefreshed != null && !maybeRefreshed.isBlank()) {
        // TOKEN_REFRESHED/CREATED vb. senaryolarda yeni token gelir -> güncelle
        licenseTokenService.storeLicenseToken(clientId, maybeRefreshed);
        return new LicenseToken(maybeRefreshed);
      }
      // aksi halde cached hâlâ geçerli kabul edilir
      return new LicenseToken(cached);
    }

    // 6) validate başarısızsa basit fallback: ISSUE dene (force=false)
    IssueAccessRequest retryIssue =
        new IssueAccessRequest()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            .signature("PLACEHOLDER_SIG")
            .checksum(request.checksum())
            .licenseKey(request.licenseKey())
            .forceTokenRefresh(Boolean.FALSE);

    ApiClientResponse<LicenseAccessResponse> retryResp =
        licenseServiceClient.issueAccess(retryIssue);

    if (retryResp.getStatus() != null
        && retryResp.getStatus() == 200
        && retryResp.getData() != null) {
      String newToken = retryResp.getData().getLicenseToken();
      if (newToken == null || newToken.isBlank()) {
        throw new IllegalStateException(
            "Licensing-service returned success but token is empty (retry).");
      }
      licenseTokenService.storeLicenseToken(clientId, newToken);
      return new LicenseToken(newToken);
    }

    // hâlâ sorun varsa yüzeye taşı
    throw new IllegalStateException("Validate+fallback issue failed: " + retryResp.getMessage());
  }
}
