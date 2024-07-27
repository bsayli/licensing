package com.c9.licensing.service.jwt;

import com.c9.licensing.model.LicenseValidationResult;

import io.jsonwebtoken.Claims;

public interface JwtService {

	Claims verifyAndExtractJwtClaims(String token);

	String generateToken(LicenseValidationResult result);

	boolean validateTokenFormat(String token);

}
