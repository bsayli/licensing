package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.LicenseValidationService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import io.github.bsayli.licensing.service.token.LicenseTokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseOrchestrationServiceImpl")
class LicenseOrchestrationServiceImplTest {

  @Mock private LicenseValidationService licenseValidationService;
  @Mock private ClientIdGenerator clientIdGenerator;
  @Mock private JwtBlacklistService jwtBlacklistService;
  @Mock private LicenseTokenManager tokenManager;

  @InjectMocks private LicenseOrchestrationServiceImpl service;

  private LicenseValidationResult baseResult() {
    return new LicenseValidationResult.Builder()
        .valid(true)
        .userId("enc-user-123")
        .appInstanceId("instance-12345678")
        .licenseStatus(LicenseStatus.ACTIVE)
        .licenseTier("PRO")
        .message("ok")
        .build();
  }

  @Test
  @DisplayName("issueToken: forceTokenRefresh=true => blacklist current token, issue & cache new")
  void issueToken_forceRefresh_true() {
    var req =
        new IssueTokenRequest(
            "crm", "1.2.3", "instance-12345678", "sig-xxxxx", "chk-xxxxx", "LK_...base64...", true);

    var result = baseResult();

    when(licenseValidationService.validateLicense(req)).thenReturn(result);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-123");
    when(tokenManager.issueAndCache(
            "client-123", result, "crm", "1.2.3", "instance-12345678", "chk-xxxxx", "sig-xxxxx"))
        .thenReturn("jwt-new");

    LicenseValidationResponse resp = service.issueToken(req);

    assertNotNull(resp);
    assertEquals("jwt-new", resp.licenseToken());
    assertEquals(io.github.bsayli.licensing.api.dto.LicenseTokenStatus.CREATED, resp.status());

    verify(licenseValidationService).validateLicense(req);
    verify(clientIdGenerator).getClientId(req);
    verify(jwtBlacklistService).addCurrentTokenToBlacklist("client-123");
    verify(tokenManager)
        .issueAndCache(
            "client-123", result, "crm", "1.2.3", "instance-12345678", "chk-xxxxx", "sig-xxxxx");
    verifyNoMoreInteractions(
        licenseValidationService, clientIdGenerator, jwtBlacklistService, tokenManager);
  }

  @Test
  @DisplayName("issueToken: forceTokenRefresh=false => do NOT blacklist, issue & cache new")
  void issueToken_forceRefresh_false() {
    var req =
        new IssueTokenRequest(
            "billing",
            "2.0.0",
            "instance-abcdef12",
            "sig-yyyyy",
            "chk-zzzzz",
            "LK_...base64...",
            false);

    var result = baseResult();

    when(licenseValidationService.validateLicense(req)).thenReturn(result);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-789");
    when(tokenManager.issueAndCache(
            "client-789",
            result,
            "billing",
            "2.0.0",
            "instance-abcdef12",
            "chk-zzzzz",
            "sig-yyyyy"))
        .thenReturn("jwt-created");

    LicenseValidationResponse resp = service.issueToken(req);

    assertNotNull(resp);
    assertEquals("jwt-created", resp.licenseToken());
    assertEquals(io.github.bsayli.licensing.api.dto.LicenseTokenStatus.CREATED, resp.status());

    verify(licenseValidationService).validateLicense(req);
    verify(clientIdGenerator).getClientId(req);
    verify(tokenManager)
        .issueAndCache(
            "client-789",
            result,
            "billing",
            "2.0.0",
            "instance-abcdef12",
            "chk-zzzzz",
            "sig-yyyyy");
    verifyNoInteractions(jwtBlacklistService);
    verifyNoMoreInteractions(licenseValidationService, clientIdGenerator, tokenManager);
  }

  @Test
  @DisplayName("validateToken: serviceStatus=TOKEN_REFRESHED => issue & cache and return refreshed")
  void validateToken_refreshed() {
    // given
    var req =
        new ValidateTokenRequest("crm", "1.2.3", "instance-12345678", "sig-xxxxx", "chk-xxxxx");
    String oldToken = "jwt-old";

    var refreshedResult =
        new LicenseValidationResult.Builder()
            .valid(true)
            .userId("enc-user-123")
            .appInstanceId("instance-12345678")
            .licenseStatus(LicenseStatus.ACTIVE)
            .licenseTier("PRO")
            .serviceStatus(ServiceErrorCode.TOKEN_REFRESHED)
            .message("refreshed")
            .build();

    when(licenseValidationService.validateLicense(req, oldToken)).thenReturn(refreshedResult);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-123");
    when(tokenManager.issueAndCache(
            "client-123",
            refreshedResult,
            "crm",
            "1.2.3",
            "instance-12345678",
            "chk-xxxxx",
            "sig-xxxxx"))
        .thenReturn("jwt-new");

    LicenseValidationResponse resp = service.validateToken(req, oldToken);

    assertNotNull(resp);
    assertEquals("jwt-new", resp.licenseToken());
    assertEquals(io.github.bsayli.licensing.api.dto.LicenseTokenStatus.REFRESHED, resp.status());

    verify(licenseValidationService).validateLicense(req, oldToken);
    verify(clientIdGenerator).getClientId(req);
    verify(tokenManager)
        .issueAndCache(
            "client-123",
            refreshedResult,
            "crm",
            "1.2.3",
            "instance-12345678",
            "chk-xxxxx",
            "sig-xxxxx");
    verifyNoMoreInteractions(licenseValidationService, clientIdGenerator, tokenManager);
    verifyNoInteractions(jwtBlacklistService);
  }

  @Test
  @DisplayName("validateToken: active (no refresh) => return ACTIVE without issuing new token")
  void validateToken_active() {

    var req =
        new ValidateTokenRequest("billing", "2.0.0", "instance-abcdef12", "sig-yyyyy", "chk-zzzzz");
    String token = "jwt-current";

    var activeResult = new LicenseValidationResult.Builder().valid(true).message("active").build();

    when(licenseValidationService.validateLicense(req, token)).thenReturn(activeResult);

    LicenseValidationResponse resp = service.validateToken(req, token);

    assertNotNull(resp);
    assertNull(resp.licenseToken());
    assertEquals(io.github.bsayli.licensing.api.dto.LicenseTokenStatus.ACTIVE, resp.status());

    verify(licenseValidationService).validateLicense(req, token);
    verifyNoInteractions(clientIdGenerator, tokenManager, jwtBlacklistService);
    verifyNoMoreInteractions(licenseValidationService);
  }
}
