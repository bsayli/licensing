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
import java.util.Random;

import com.c9.licensing.service.jwt.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtServiceImpl implements JwtService {

	private static final Random random = new Random();
	private static final long MAX_JITTER = 10000; 

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
	public String generateToken(String clientId, String licenseTier, String licenseStatus) {
		Instant now = Instant.now();
		long jitter = random.nextLong(MAX_JITTER);
		Integer tokenExpirationWithJitter = (tokenExpirationInMinute * 60) + (int) (jitter / 1000);
		Instant expiry = now.plus(tokenExpirationWithJitter, ChronoUnit.SECONDS);

		return Jwts.builder()
				.subject(clientId)
				.claim("licenseStatus", licenseStatus)
				.claim("licenseTier", licenseTier)
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