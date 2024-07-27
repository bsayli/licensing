package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.errors.TokenExpiredException;
import com.c9.licensing.model.errors.TokenForbiddenAccessException;
import com.c9.licensing.model.errors.TokenInvalidException;
import com.c9.licensing.model.errors.TokenIsTooOldForRefreshException;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseTokenValidationService;
import com.c9.licensing.service.jwt.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.MalformedKeyException;
import io.jsonwebtoken.security.SignatureException;

@Service
public class LicenseTokenValidationServiceImpl implements LicenseTokenValidationService {

	private final JwtService jwtUtil;
	private final LicenseClientCacheManagementService clientCacheManagementService;

	public LicenseTokenValidationServiceImpl(JwtService jwtUtil, LicenseClientCacheManagementService clientCacheManagementService) {
		this.jwtUtil = jwtUtil;
		this.clientCacheManagementService = clientCacheManagementService;
	}

	public void validateToken(String token, String requestedInstanceId) {
		try {
			boolean isValidFormat = jwtUtil.validateTokenFormat(token);
			if(!isValidFormat) {
				throw new TokenInvalidException(TOKEN_INVALID);
			}
			
			Claims claims = jwtUtil.verifyAndExtractJwtClaims(token);

			String appInstanceId = claims.getSubject();
			if (!appInstanceId.equals(requestedInstanceId)) {
				throw new TokenForbiddenAccessException(TOKEN_INVALID_ACCESS);
			}

			boolean isTokenExpired = isTokenExpired(claims);
			if (isTokenExpired) {
				validateAndThrowTokenException(token);
			}

		} catch (ExpiredJwtException e) {
			validateAndThrowTokenException(token);
		} catch (SignatureException | MalformedKeyException se) {
			throw new TokenInvalidException(TOKEN_INVALID, se);
		} catch (TokenForbiddenAccessException se) {
			throw new TokenForbiddenAccessException(TOKEN_INVALID_ACCESS, se);
		} catch (Exception e) {
			logger.error(ERROR_DURING_TOKEN_VALIDATION, e);
			throw new TokenInvalidException(ERROR_DURING_TOKEN_VALIDATION, e);
		}

	}

	private void validateAndThrowTokenException(String token) {
		Optional<String> userIdOpt = clientCacheManagementService.getUserIdAndEvictToken(token);
		if (userIdOpt.isPresent()) {
			String userId = userIdOpt.get();
			throw new TokenExpiredException(userId, TOKEN_HAS_EXPIRED);
		} else {
			throw new TokenIsTooOldForRefreshException(TOKEN_IS_TOO_OLD_FOR_REFRESH);
		}
	}

	private boolean isTokenExpired(Claims claims) {
		long expirationTime = claims.getExpiration().getTime();
		long currentTime = System.currentTimeMillis();
		long timeRemaining = expirationTime - currentTime;
		return timeRemaining <= 0;
	}

}
