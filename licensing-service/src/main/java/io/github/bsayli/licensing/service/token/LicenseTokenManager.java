package io.github.bsayli.licensing.service.token;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.service.ClientSessionCache;
import io.github.bsayli.licensing.service.jwt.JwtService;
import org.springframework.stereotype.Component;

@Component
public class LicenseTokenManager {

  private final JwtService jwtService;
  private final ClientSessionCache cache;

  public LicenseTokenManager(JwtService jwtService, ClientSessionCache cache) {
    this.jwtService = jwtService;
    this.cache = cache;
  }

  public String issueAndCache(
      String clientId,
      LicenseValidationResult result,
      String serviceId,
      String serviceVersion,
      String instanceId,
      String checksum,
      String signature) {

    String token = jwtService.generateToken(clientId, result.licenseTier(), result.licenseStatus());

    ClientInfo clientInfo =
        new ClientInfo.Builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVersion)
            .instanceId(instanceId)
            .checksum(checksum)
            .signature(signature)
            .encUserId(result.userId())
            .licenseToken(token)
            .build();

    cache.put(clientInfo);
    return token;
  }
}
