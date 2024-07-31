package com.c9.licensing.service.jwt.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.service.jwt.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtServiceImpl implements JwtService {

	private final PrivateKey privateKey;
	private final PublicKey publicKey;
	private final Integer tokenExpirationInMinute;

	public JwtServiceImpl(String privateKeyStr, String publicKeyStr, Integer tokenExpirationInMinute)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyStr);
		KeyFactory keyFactory = KeyFactory.getInstance("EdDSA", "BC");
		this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));

		byte[] decodedPublicKey = Base64.getDecoder().decode(publicKeyStr);
		this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));

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
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(privateKey)
				.compact();
	}

	@Override
	public Claims verifyAndExtractJwtClaims(String token) {
		return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
	}

	@Override
	public boolean validateTokenFormat(String token) {
		if (token == null)
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