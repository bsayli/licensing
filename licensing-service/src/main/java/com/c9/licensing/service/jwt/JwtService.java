package com.c9.licensing.service.jwt;

import io.jsonwebtoken.Claims;

public interface JwtService {

	Claims verifyAndExtractJwtClaims(String token);

	String generateToken(String clientId, String licenseTier, String licenseStatus);

	boolean validateTokenFormat(String token);

}
