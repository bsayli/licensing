package com.c9.licensing.service.impl;

import org.springframework.stereotype.Service;

import com.c9.licensing.errors.TokenExpiredException;
import com.c9.licensing.errors.TokenInvalidAccessException;
import com.c9.licensing.errors.TokenInvalidException;
import com.c9.licensing.security.JwtUtil;
import com.c9.licensing.service.TokenCacheService;
import com.c9.licensing.service.TokenValidationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.MalformedKeyException;
import io.jsonwebtoken.security.SignatureException;

@Service
public class TokenValidationServiceImpl implements TokenValidationService{ 

	private JwtUtil jwtUtil;
	private TokenCacheService tokenCacheService;

	public void validateToken(String token, String requestedInstanceId) {
		try {
			Claims claims = jwtUtil.validateToken(token);

			String appInstanceId = claims.get("appInstanceId", String.class);
			if (!appInstanceId.equals(requestedInstanceId)) {
				throw new TokenInvalidAccessException(TOKEN_INVALID_ACCESS);
			}

			boolean isTokenExpired = isTokenExpired(claims);
			if (isTokenExpired) {
				validateAndThrowTokenException(token, claims);
			}

		} catch (ExpiredJwtException e) {
			validateAndThrowTokenException(token, e.getClaims());
		} catch (SignatureException | MalformedKeyException se) {
			throw new TokenInvalidException(TOKEN_INVALID, se);
		} catch (Exception e) {
			logger.error(ERROR_DURING_TOKEN_VALIDATION, e);
			throw new TokenInvalidException(ERROR_DURING_TOKEN_VALIDATION, e);
		}

	}

	private void validateAndThrowTokenException(String token, Claims claims) {
		boolean tokenValidAndInvalidated = tokenCacheService.isTokenValidAndInvalidate(token);
		if (tokenValidAndInvalidated) {
			throw new TokenExpiredException(claims.getSubject(), TOKEN_HAS_EXPIRED);
		} else {
			throw new TokenInvalidException(TOKEN_INVALID);
		}
	}

	private boolean isTokenExpired(Claims claims) {
		long expirationTime = claims.getExpiration().getTime();
		long currentTime = System.currentTimeMillis();
		long timeRemaining = expirationTime - currentTime;
		return timeRemaining <= 0;
	}

}
