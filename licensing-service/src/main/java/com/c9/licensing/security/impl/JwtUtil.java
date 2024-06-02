package com.c9.licensing.security.impl;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import com.c9.licensing.errors.TokenInvalidException;
import com.c9.licensing.model.LicenseValidationResult;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

	private final Key secretKey;

	public JwtUtil(@Value("${license.jwt.secret}") String secretKeyString) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(LicenseValidationResult result) {
		Instant now = Instant.now();
		Instant expiry = now.plus(1, ChronoUnit.MINUTES);

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

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    

}