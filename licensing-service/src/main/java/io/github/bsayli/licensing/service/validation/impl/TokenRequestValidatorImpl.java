package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.exception.request.InvalidRequestException;
import io.github.bsayli.licensing.service.exception.token.TokenAccessDeniedException;
import io.github.bsayli.licensing.service.exception.token.TokenExpiredException;
import io.github.bsayli.licensing.service.exception.token.TokenInvalidException;
import io.github.bsayli.licensing.service.exception.token.TokenIsTooOldForRefreshException;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import io.github.bsayli.licensing.service.jwt.JwtService;
import io.github.bsayli.licensing.service.validation.TokenRequestValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TokenRequestValidatorImpl implements TokenRequestValidator {

  private final JwtService jwtService;
  private final ClientSessionCacheService cacheService;
  private final JwtBlacklistService jwtBlacklistService;
  private final ClientIdGenerator clientIdGenerator;
  private final SignatureValidator signatureValidator;

  public TokenRequestValidatorImpl(
      JwtService jwtService,
      ClientSessionCacheService cacheService,
      ClientIdGenerator clientIdGenerator,
      JwtBlacklistService jwtBlacklistService,
      SignatureValidator signatureValidator) {
    this.jwtService = jwtService;
    this.cacheService = cacheService;
    this.jwtBlacklistService = jwtBlacklistService;
    this.clientIdGenerator = clientIdGenerator;
    this.signatureValidator = signatureValidator;
  }

  @Override
  public void assertValid(ValidateAccessRequest request, String token) {
    signatureValidator.validate(request, token);

    if (!jwtService.validateTokenFormat(token)) {
      throw new TokenInvalidException();
    }
    if (jwtBlacklistService.isBlacklisted(token)) {
      throw new TokenInvalidException();
    }

    assertRequestMatchesCache(request, token);

    try {
      Claims claims = jwtService.verifyAndExtractJwtClaims(token);

      String subjectClientId = claims.getSubject();
      String requestedClientId = clientIdGenerator.getClientId(request);
      if (!Objects.equals(subjectClientId, requestedClientId)) {
        throw new TokenAccessDeniedException();
      }

      if (isExpired(claims)) {
        throwTokenExceptionBasedOnCache(request, token);
      }
    } catch (ExpiredJwtException e) {
      throwTokenExceptionBasedOnCache(request, token);
    }
  }

  private void assertRequestMatchesCache(ValidateAccessRequest request, String token) {
    String clientId = clientIdGenerator.getClientId(request);
    Optional<ClientSessionSnapshot> cachedOpt = cacheService.find(clientId);
    if (cachedOpt.isEmpty()) {
      return;
    }

    ClientSessionSnapshot cached = cachedOpt.get();

    boolean tokenMatches = Objects.equals(cached.licenseToken(), token);
    if (!tokenMatches) {
      throw new TokenInvalidException();
    }

    boolean serviceMatches = Objects.equals(cached.serviceId(), request.serviceId());
    boolean versionMatches = Objects.equals(cached.serviceVersion(), request.serviceVersion());
    boolean checksumMatches = Objects.equals(cached.checksum(), request.checksum());

    if (!(serviceMatches && versionMatches && checksumMatches)) {
      throw new InvalidRequestException();
    }
  }

  private void throwTokenExceptionBasedOnCache(ValidateAccessRequest request, String token) {
    String clientId = clientIdGenerator.getClientId(request);
    Optional<ClientSessionSnapshot> cachedOpt = cacheService.find(clientId);

    if (cachedOpt.isEmpty()) {
      throw new TokenIsTooOldForRefreshException();
    }

    ClientSessionSnapshot cached = cachedOpt.get();
    if (Objects.equals(cached.licenseToken(), token)) {
      throw new TokenExpiredException(cached.encUserId());
    } else {
      throw new TokenInvalidException();
    }
  }

  private boolean isExpired(Claims claims) {
    long now = System.currentTimeMillis();
    return claims.getExpiration() == null || claims.getExpiration().getTime() <= now;
  }
}
