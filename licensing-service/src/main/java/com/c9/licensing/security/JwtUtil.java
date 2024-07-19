package com.c9.licensing.security;

import com.c9.licensing.model.LicenseValidationResult;

import io.jsonwebtoken.Claims;

public interface JwtUtil {

	Claims verifyAndExtractJwtClaims(String token);

	String generateToken(LicenseValidationResult result);

	boolean validateTokenFormat(String token);

}
