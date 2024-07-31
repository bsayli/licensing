package com.c9.licensing.service.validation.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.generator.ClientIdGenerator;
import com.c9.licensing.model.ClientCachedLicenseData;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.errors.TokenExpiredException;
import com.c9.licensing.model.errors.TokenForbiddenAccessException;
import com.c9.licensing.model.errors.TokenInvalidException;
import com.c9.licensing.model.errors.TokenIsTooOldForRefreshException;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.jwt.JwtService;
import com.c9.licensing.service.validation.LicenseTokenValidationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.MalformedKeyException;
import io.jsonwebtoken.security.SignatureException;

@Service
public class LicenseTokenValidationServiceImpl implements LicenseTokenValidationService {

	private final JwtService jwtUtil;
	private final LicenseClientCacheManagementService cacheService;
	private final ClientIdGenerator clientIdGenerator;

	public LicenseTokenValidationServiceImpl(JwtService jwtUtil, LicenseClientCacheManagementService cacheService, ClientIdGenerator clientIdGenerator) {
		this.jwtUtil = jwtUtil;
		this.cacheService = cacheService;
		this.clientIdGenerator = clientIdGenerator;
	}

	public void validateToken(LicenseValidationRequest request) {
		try {
			boolean isValidFormat = jwtUtil.validateTokenFormat(request.licenseToken());
			if(!isValidFormat) {
				throw new TokenInvalidException(MESSAGE_TOKEN_INVALID);
			}
			
			Claims claims = jwtUtil.verifyAndExtractJwtClaims(request.licenseToken());

			String clientId = claims.getSubject();
			String requestedClientId = clientIdGenerator.getClientId(request.serviceId(), request.serviceVersion(), request.instanceId());
			if (!clientId.equals(requestedClientId)) {
				throw new TokenForbiddenAccessException(MESSAGE_TOKEN_INVALID_ACCESS);
			}

			boolean isTokenExpired = isTokenExpired(claims);
			if (isTokenExpired) {
				validateAndThrowTokenException(request);
			}

		} catch (ExpiredJwtException e) {
			validateAndThrowTokenException(request);
		} catch (SignatureException | MalformedKeyException se) {
			throw new TokenInvalidException(MESSAGE_TOKEN_INVALID, se);
		} catch (TokenForbiddenAccessException se) {
			throw new TokenForbiddenAccessException(MESSAGE_TOKEN_INVALID_ACCESS, se);
		} catch (Exception e) {
			logger.error(MESSAGE_ERROR_DURING_TOKEN_VALIDATION, e);
			throw new TokenInvalidException(MESSAGE_ERROR_DURING_TOKEN_VALIDATION, e);
		}
	}

	private void validateAndThrowTokenException(LicenseValidationRequest request) {
		String clientId = clientIdGenerator.getClientId(request.serviceId(), request.serviceVersion(), request.instanceId());
		Optional<ClientCachedLicenseData> cachedLicenseDataOpt = cacheService.getClientCachedLicenseData(clientId);
		if (cachedLicenseDataOpt.isPresent()) {
			ClientCachedLicenseData data = cachedLicenseDataOpt.get();
			throw new TokenExpiredException(data.getEncUserId(), MESSAGE_TOKEN_HAS_EXPIRED);
		} else {
			throw new TokenIsTooOldForRefreshException(MESSAGE_TOKEN_IS_TOO_OLD_FOR_REFRESH);
		}
	}

	private boolean isTokenExpired(Claims claims) {
		long expirationTime = claims.getExpiration().getTime();
		long currentTime = System.currentTimeMillis();
		long timeRemaining = expirationTime - currentTime;
		return timeRemaining <= 0;
	}

}
