package io.github.bsayli.licensing.service.jwt;

import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.jsonwebtoken.Claims;

public interface JwtService {

  Claims verifyAndExtractJwtClaims(String token);

  String generateToken(String clientId, String licenseTier, LicenseStatus licenseStatus);

  boolean validateTokenFormat(String token);
}
