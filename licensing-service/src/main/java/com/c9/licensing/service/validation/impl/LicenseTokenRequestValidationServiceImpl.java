package com.c9.licensing.service.validation.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.generator.ClientIdGenerator;
import com.c9.licensing.model.ClientCachedLicenseData;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.errors.InvalidRequestException;
import com.c9.licensing.model.errors.TokenExpiredException;
import com.c9.licensing.model.errors.TokenForbiddenAccessException;
import com.c9.licensing.model.errors.TokenInvalidException;
import com.c9.licensing.model.errors.TokenIsTooOldForRefreshException;
import com.c9.licensing.security.SignatureValidator;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.jwt.JwtBlacklistService;
import com.c9.licensing.service.jwt.JwtService;
import com.c9.licensing.service.validation.LicenseTokenRequestValidationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.MalformedKeyException;
import io.jsonwebtoken.security.SignatureException;

@Service
public class LicenseTokenRequestValidationServiceImpl implements LicenseTokenRequestValidationService {

	private final JwtService jwtUtil;
	private final LicenseClientCacheManagementService cacheService;
	private final JwtBlacklistService jwtBlacklistService;
	private final ClientIdGenerator clientIdGenerator;
	private final SignatureValidator signatureValidator;

	public LicenseTokenRequestValidationServiceImpl(JwtService jwtUtil,
			LicenseClientCacheManagementService cacheService, ClientIdGenerator clientIdGenerator,
			JwtBlacklistService jwtBlacklistService, SignatureValidator signatureValidator) {
		this.jwtUtil = jwtUtil;
		this.cacheService = cacheService;
		this.jwtBlacklistService = jwtBlacklistService;
		this.clientIdGenerator = clientIdGenerator;
		this.signatureValidator = signatureValidator;
	}

	public void validateTokenRequest(LicenseValidationRequest request) {
		signatureValidator.validateSignature(request);
		
		boolean isValidFormat = jwtUtil.validateTokenFormat(request.licenseToken());
		if (!isValidFormat) {
			throw new TokenInvalidException(MESSAGE_TOKEN_INVALID);
		}

		boolean isBlackListed = jwtBlacklistService.isBlackListed(request.licenseToken());
		if (isBlackListed) {
			throw new TokenInvalidException(MESSAGE_TOKEN_INVALIDATED_BY_FORCE_REFRESH);
		}
		
		checkTokenRequestWithCachedData(request);
		
		try {
			Claims claims = jwtUtil.verifyAndExtractJwtClaims(request.licenseToken());
			String clientId = claims.getSubject();
			String requestedClientId = clientIdGenerator.getClientId(request);
			
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
	
	private void checkTokenRequestWithCachedData(LicenseValidationRequest request) {
		String clientId = clientIdGenerator.getClientId(request);
		Optional<ClientCachedLicenseData> cachedLicenseDataOpt = cacheService.getClientCachedLicenseData(clientId);
		if (cachedLicenseDataOpt.isPresent()) {
			ClientCachedLicenseData cachedData = cachedLicenseDataOpt.get();
			boolean isTokenEqual = Objects.equals(cachedData.getLicenseToken(), request.licenseToken());
			boolean isServiceIdEqual = Objects.equals(cachedData.getServiceId(), request.serviceId());
			boolean isServiceVersionEqual = Objects.equals(cachedData.getServiceVersion(), request.serviceVersion());
			boolean isChecksumEqual = Objects.equals(cachedData.getChecksum(), request.checksum());
			boolean isValidRequest = isTokenEqual && isServiceIdEqual && isServiceVersionEqual && isChecksumEqual;
			
			if(!isTokenEqual) {
				throw new TokenInvalidException(MESSAGE_TOKEN_INVALID);
			}
			
			if(!isValidRequest) {
				throw new InvalidRequestException(MESSAGE_INVALID_REQUEST);
			}
		}
	}

	private void validateAndThrowTokenException(LicenseValidationRequest request) {
		String clientId = clientIdGenerator.getClientId(request);
		Optional<ClientCachedLicenseData> cachedLicenseDataOpt = cacheService.getClientCachedLicenseData(clientId);
		if (cachedLicenseDataOpt.isPresent()) {
			ClientCachedLicenseData data = cachedLicenseDataOpt.get();
			if(data.getLicenseToken().equals(request.licenseToken())) {
				throw new TokenExpiredException(data.getEncUserId(), MESSAGE_TOKEN_HAS_EXPIRED);
			}else {
				throw new TokenInvalidException(MESSAGE_TOKEN_INVALID);
			}
			
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
