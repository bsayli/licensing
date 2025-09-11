package io.github.bsayli.licensing.service.token;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.jwt.JwtService;
import org.springframework.stereotype.Component;

@Component
public class LicenseTokenManager {

  private final JwtService jwtService;
  private final ClientSessionCacheService cache;

  public LicenseTokenManager(JwtService jwtService, ClientSessionCacheService cache) {
    this.jwtService = jwtService;
    this.cache = cache;
  }

  public String issueAndCache(LicenseTokenIssueRequest req) {
    LicenseValidationResult result = req.result();
    String token =
        jwtService.generateToken(req.clientId(), result.licenseTier(), result.licenseStatus());

    ClientInfo clientInfo =
        new ClientInfo.Builder()
            .serviceId(req.serviceId())
            .serviceVersion(req.serviceVersion())
            .instanceId(req.instanceId())
            .checksum(req.checksum())
            .signature(req.signature())
            .encUserId(result.userId())
            .licenseToken(token)
            .build();

    cache.put(clientInfo);
    return token;
  }
}
