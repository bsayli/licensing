package com.c9.licensing.security;

import com.c9.licensing.model.LicenseValidationResult;

import io.jsonwebtoken.Claims;

public interface JwtUtil {

	Claims validateToken(String token);

	String generateToken(LicenseValidationResult result);

}
