package com.c9.licensing.security.impl;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.security.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JwtUtilImpl implements JwtUtil {

	private final Key secretKey;
	private final Integer tokenExpirationInMinute;

	public JwtUtilImpl(String secretKeyString, Integer tokenExpirationInMinute) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.tokenExpirationInMinute = tokenExpirationInMinute;
	}

	@Override
	public String generateToken(LicenseValidationResult result) {
		Instant now = Instant.now();
		Instant expiry = now.plus(tokenExpirationInMinute, ChronoUnit.MINUTES);

		return Jwts.builder()
				.subject(result.appInstanceId())
				.claim("licenseStatus", result.licenseStatus())
				.claim("licenseTier", result.licenseTier())
				.issuedAt(Date.from(now)).expiration(Date.from(expiry))
				.signWith(secretKey).compact();
	}

	@Override
	public Claims verifyAndExtractJwtClaims(String token) {
		return Jwts.parser().verifyWith((SecretKey) secretKey).build().parseSignedClaims(token).getPayload();
	}

	@Override
	public boolean validateTokenFormat(String token) {
		if(token == null)
			return false;
		
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			return false;
		}

		for (String part : parts) {
			try {
				Base64.getUrlDecoder().decode(part);
			} catch (IllegalArgumentException e) {
				return false;
			}
		}

		String header = new String(Base64.getUrlDecoder().decode(parts[0]));
		return header.contains("alg");
	}

}