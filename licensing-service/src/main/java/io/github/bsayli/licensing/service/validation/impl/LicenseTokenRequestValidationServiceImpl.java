package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.model.errors.InvalidRequestException;
import io.github.bsayli.licensing.model.errors.TokenExpiredException;
import io.github.bsayli.licensing.model.errors.TokenForbiddenAccessException;
import io.github.bsayli.licensing.model.errors.TokenInvalidException;
import io.github.bsayli.licensing.model.errors.TokenIsTooOldForRefreshException;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.service.LicenseClientCacheManagementService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import io.github.bsayli.licensing.service.jwt.JwtService;
import io.github.bsayli.licensing.service.validation.LicenseTokenRequestValidationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.MalformedKeyException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LicenseTokenRequestValidationServiceImpl
    implements LicenseTokenRequestValidationService {

  private static final Logger log =
      LoggerFactory.getLogger(LicenseTokenRequestValidationServiceImpl.class);

  private final JwtService jwtUtil;
  private final LicenseClientCacheManagementService cacheService;
  private final JwtBlacklistService jwtBlacklistService;
  private final ClientIdGenerator clientIdGenerator;
  private final SignatureValidator signatureValidator;

  public LicenseTokenRequestValidationServiceImpl(
      JwtService jwtUtil,
      LicenseClientCacheManagementService cacheService,
      ClientIdGenerator clientIdGenerator,
      JwtBlacklistService jwtBlacklistService,
      SignatureValidator signatureValidator) {
    this.jwtUtil = jwtUtil;
    this.cacheService = cacheService;
    this.jwtBlacklistService = jwtBlacklistService;
    this.clientIdGenerator = clientIdGenerator;
    this.signatureValidator = signatureValidator;
  }

  @Override
  public void validateTokenRequest(ValidateTokenRequest request, String token) {
    signatureValidator.validate(request, token);

    if (!jwtUtil.validateTokenFormat(token)) {
      throw new TokenInvalidException();
    }

    if (jwtBlacklistService.isBlackListed(token)) {
      throw new TokenInvalidException();
    }

    checkTokenRequestWithCachedData(request, token);

    try {
      Claims claims = jwtUtil.verifyAndExtractJwtClaims(token);
      String clientId = claims.getSubject();
      String requestedClientId = clientIdGenerator.getClientId(request);

      if (!clientId.equals(requestedClientId)) {
        throw new TokenForbiddenAccessException();
      }

      if (isTokenExpired(claims)) {
        validateAndThrowTokenException(request, token);
      }
    } catch (ExpiredJwtException e) {
      validateAndThrowTokenException(request, token);
    } catch (SignatureException | MalformedKeyException e) {
      throw new TokenInvalidException(e);
    } catch (TokenForbiddenAccessException e) {
      throw e;
    } catch (Exception e) {
      log.error("Token validation failed", e);
      throw new TokenInvalidException(e);
    }
  }

  private void checkTokenRequestWithCachedData(ValidateTokenRequest request, String token) {
    String clientId = clientIdGenerator.getClientId(request);
    Optional<ClientCachedLicenseData> cachedOpt = cacheService.getClientCachedLicenseData(clientId);

    if (cachedOpt.isPresent()) {
      ClientCachedLicenseData cached = cachedOpt.get();

      boolean isTokenEqual = Objects.equals(cached.getLicenseToken(), token);
      boolean isServiceIdEqual = Objects.equals(cached.getServiceId(), request.serviceId());
      boolean isServiceVersionEqual =
          Objects.equals(cached.getServiceVersion(), request.serviceVersion());
      boolean isChecksumEqual = Objects.equals(cached.getChecksum(), request.checksum());

      boolean isValidRequest =
          isTokenEqual && isServiceIdEqual && isServiceVersionEqual && isChecksumEqual;

      if (!isTokenEqual) {
        throw new TokenInvalidException();
      }
      if (!isValidRequest) {
        throw new InvalidRequestException();
      }
    }
  }

  private void validateAndThrowTokenException(ValidateTokenRequest request, String token) {
    String clientId = clientIdGenerator.getClientId(request);
    Optional<ClientCachedLicenseData> cachedOpt = cacheService.getClientCachedLicenseData(clientId);

    if (cachedOpt.isPresent()) {
      ClientCachedLicenseData data = cachedOpt.get();
      if (Objects.equals(data.getLicenseToken(), token)) {
        throw new TokenExpiredException(data.getEncUserId());
      } else {
        throw new TokenInvalidException();
      }
    } else {
      throw new TokenIsTooOldForRefreshException();
    }
  }

  private boolean isTokenExpired(Claims claims) {
    long expirationTime = claims.getExpiration().getTime();
    long currentTime = System.currentTimeMillis();
    return (expirationTime - currentTime) <= 0;
  }
}
