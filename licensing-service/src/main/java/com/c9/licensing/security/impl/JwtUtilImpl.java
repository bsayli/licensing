package com.c9.licensing.security.impl;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
				.subject(result.userId())
				.claim("licenseStatus", result.licenseStatus())
				.claim("licenseTier", result.licenseTier())
				.claim("message", result.message())
				.claim("appInstanceId", result.appInstanceId())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(secretKey).compact();
	}

    @Override
	public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    

}