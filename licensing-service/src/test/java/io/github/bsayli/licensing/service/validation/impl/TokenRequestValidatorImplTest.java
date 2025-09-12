package io.github.bsayli.licensing.service.validation.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: TokenRequestValidatorImpl")
class TokenRequestValidatorImplTest {

  @Mock private JwtService jwtService;
  @Mock private ClientSessionCacheService cache;
  @Mock private JwtBlacklistService blacklist;
  @Mock private ClientIdGenerator clientIdGenerator;
  @Mock private SignatureValidator signatureValidator;

  @InjectMocks private TokenRequestValidatorImpl validator;

  private static ValidateAccessRequest req() {
    return new ValidateAccessRequest("inst-12345678", "chk", "crm", "1.2.3", "sig");
  }

  private static ClientSessionSnapshot cached(
      String token, String serviceId, String version, String checksum, String encUserId) {
    return new ClientSessionSnapshot.Builder()
        .licenseToken(token)
        .encUserId(encUserId)
        .serviceId(serviceId)
        .serviceVersion(version)
        .checksum(checksum)
        .build();
  }

  private static Claims claims(String subject, long expMillis) {
    Map<String, Object> map = new HashMap<>();
    map.put("sub", subject);
    map.put("exp", new java.util.Date(expMillis));
    return new io.jsonwebtoken.impl.DefaultClaims(map);
  }

  @Test
  @DisplayName(
      "assertValid passes for valid signature, format, cache match, subject match, not expired")
  void assertValid_happyPath() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(cached(token, "crm", "1.2.3", "chk", "encU"));
    when(jwtService.verifyAndExtractJwtClaims(token))
        .thenReturn(claims("client-1", System.currentTimeMillis() + 60_000));

    assertDoesNotThrow(() -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName("assertValid throws TokenInvalidException when format is invalid")
  void assertValid_invalidFormat() {
    String token = "bad";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(false);

    assertThrows(TokenInvalidException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName("assertValid throws TokenInvalidException when token is blacklisted")
  void assertValid_blacklisted() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(true);

    assertThrows(TokenInvalidException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName("assertValid throws TokenInvalidException when cache has different token")
  void assertValid_cacheTokenMismatch() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(cached("x.y.z", "crm", "1.2.3", "chk", "encU"));

    assertThrows(TokenInvalidException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName("assertValid throws InvalidRequestException when service context mismatches cache")
  void assertValid_contextMismatch() {
    String token = "a.b.c";
    ValidateAccessRequest request =
        new ValidateAccessRequest("inst-1", "chk", "crm", "9.9.9", "sig");

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(cached(token, "crm", "1.2.3", "chk", "encU"));

    assertThrows(InvalidRequestException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName("assertValid throws TokenAccessDeniedException when JWT subject != clientId")
  void assertValid_subjectMismatch() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(cached(token, "crm", "1.2.3", "chk", "encU"));
    when(jwtService.verifyAndExtractJwtClaims(token))
        .thenReturn(claims("other-client", System.currentTimeMillis() + 60_000));

    assertThrows(TokenAccessDeniedException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName("assertValid throws TokenExpiredException when expired and cache has same token")
  void assertValid_expired_sameToken() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(cached(token, "crm", "1.2.3", "chk", "encU"));
    when(jwtService.verifyAndExtractJwtClaims(token))
        .thenReturn(claims("client-1", System.currentTimeMillis() - 1));

    assertThrows(TokenExpiredException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName(
      "assertValid throws TokenIsTooOldForRefreshException when expired and cache is empty")
  void assertValid_expired_noCache() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(null); // cache miss
    when(jwtService.verifyAndExtractJwtClaims(token))
        .thenReturn(claims("client-1", System.currentTimeMillis() - 1));

    assertThrows(
        TokenIsTooOldForRefreshException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }

  @Test
  @DisplayName(
      "assertValid handles ExpiredJwtException by consulting cache and throwing TokenExpiredException")
  void assertValid_expiredJwtException_path() {
    String token = "a.b.c";
    ValidateAccessRequest request = req();

    when(jwtService.validateTokenFormat(token)).thenReturn(true);
    when(blacklist.isBlacklisted(token)).thenReturn(false);
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(cached(token, "crm", "1.2.3", "chk", "encU"));
    when(jwtService.verifyAndExtractJwtClaims(token)).thenThrow(mock(ExpiredJwtException.class));

    assertThrows(TokenExpiredException.class, () -> validator.assertValid(request, token));
    verify(signatureValidator).validate(request, token);
  }
}
