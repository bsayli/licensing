package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.LicenseAccessStatus;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.LicenseValidationService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import io.github.bsayli.licensing.service.token.LicenseTokenIssueRequest;
import io.github.bsayli.licensing.service.token.LicenseTokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
  @DisplayName("issueAccess: forceTokenRefresh=true => blacklist current token, issue & cache new")
  void issueAccess_forceRefresh_true() {
    var req =
        new IssueAccessRequest(
            "LK_...base64...", "instance-12345678", "chk-xxxxx", "crm", "1.2.3", "sig-xxxxx", true);

    var result = baseResult();

    when(licenseValidationService.validateLicense(req)).thenReturn(result);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-123");
    when(tokenManager.issueAndCache(any(LicenseTokenIssueRequest.class))).thenReturn("jwt-new");

    LicenseAccessResponse resp = service.issueAccess(req);

    assertNotNull(resp);
    assertEquals("jwt-new", resp.licenseToken());
    assertEquals(LicenseAccessStatus.TOKEN_CREATED, resp.status());

    ArgumentCaptor<LicenseTokenIssueRequest> captor =
        ArgumentCaptor.forClass(LicenseTokenIssueRequest.class);
    verify(tokenManager).issueAndCache(captor.capture());
    var cmd = captor.getValue();
    assertEquals("client-123", cmd.clientId());
    assertEquals("crm", cmd.serviceId());
    assertEquals("1.2.3", cmd.serviceVersion());
    assertEquals("instance-12345678", cmd.instanceId());
    assertEquals("chk-xxxxx", cmd.checksum());
    assertEquals("sig-xxxxx", cmd.signature());

    verify(licenseValidationService).validateLicense(req);
    verify(clientIdGenerator).getClientId(req);
    verify(jwtBlacklistService).addCurrentTokenToBlacklist("client-123");
    verifyNoMoreInteractions(
        licenseValidationService, clientIdGenerator, jwtBlacklistService, tokenManager);
  }

  @Test
  @DisplayName("issueAccess: forceTokenRefresh=false => do NOT blacklist, issue & cache new")
  void issueAccess_forceRefresh_false() {
    var req =
        new IssueAccessRequest(
            "LK_...base64...",
            "instance-abcdef12",
            "chk-zzzzz",
            "billing",
            "2.0.0",
            "sig-yyyyy",
            false);

    var result = baseResult();

    when(licenseValidationService.validateLicense(req)).thenReturn(result);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-789");
    when(tokenManager.issueAndCache(any(LicenseTokenIssueRequest.class))).thenReturn("jwt-created");

    LicenseAccessResponse resp = service.issueAccess(req);

    assertNotNull(resp);
    assertEquals("jwt-created", resp.licenseToken());
    assertEquals(LicenseAccessStatus.TOKEN_CREATED, resp.status());

    ArgumentCaptor<LicenseTokenIssueRequest> captor =
        ArgumentCaptor.forClass(LicenseTokenIssueRequest.class);
    verify(tokenManager).issueAndCache(captor.capture());
    var cmd = captor.getValue();
    assertEquals("client-789", cmd.clientId());
    assertEquals("billing", cmd.serviceId());
    assertEquals("2.0.0", cmd.serviceVersion());
    assertEquals("instance-abcdef12", cmd.instanceId());
    assertEquals("chk-zzzzz", cmd.checksum());
    assertEquals("sig-yyyyy", cmd.signature());

    verify(licenseValidationService).validateLicense(req);
    verify(clientIdGenerator).getClientId(req);
    verifyNoInteractions(jwtBlacklistService);
    verifyNoMoreInteractions(licenseValidationService, clientIdGenerator, tokenManager);
  }

  @Test
  @DisplayName(
      "validateAccess: serviceStatus=TOKEN_REFRESHED => issue & cache and return refreshed")
  void validateAccess_refreshed() {
    var req =
        new ValidateAccessRequest("instance-12345678", "chk-xxxxx", "crm", "1.2.3", "sig-xxxxx");
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
    when(tokenManager.issueAndCache(any(LicenseTokenIssueRequest.class))).thenReturn("jwt-new");

    LicenseAccessResponse resp = service.validateAccess(req, oldToken);

    assertNotNull(resp);
    assertEquals("jwt-new", resp.licenseToken());
    assertEquals(LicenseAccessStatus.TOKEN_REFRESHED, resp.status());

    ArgumentCaptor<LicenseTokenIssueRequest> captor =
        ArgumentCaptor.forClass(LicenseTokenIssueRequest.class);
    verify(tokenManager).issueAndCache(captor.capture());
    var cmd = captor.getValue();
    assertEquals("client-123", cmd.clientId());
    assertEquals("crm", cmd.serviceId());
    assertEquals("1.2.3", cmd.serviceVersion());
    assertEquals("instance-12345678", cmd.instanceId());
    assertEquals("chk-xxxxx", cmd.checksum());
    assertEquals("sig-xxxxx", cmd.signature());

    verify(licenseValidationService).validateLicense(req, oldToken);
    verify(clientIdGenerator).getClientId(req);
    verifyNoMoreInteractions(licenseValidationService, clientIdGenerator, tokenManager);
    verifyNoInteractions(jwtBlacklistService);
  }

  @Test
  @DisplayName("validateAccess: active (no refresh) => return ACTIVE without issuing new token")
  void validateAccess_active() {
    var req =
        new ValidateAccessRequest(
            "instance-abcdef12", "chk-zzzzz", "billing", "2.0.0", "sig-yyyyy");
    String token = "jwt-current";

    var activeResult = new LicenseValidationResult.Builder().valid(true).message("active").build();

    when(licenseValidationService.validateLicense(req, token)).thenReturn(activeResult);

    LicenseAccessResponse resp = service.validateAccess(req, token);

    assertNotNull(resp);
    assertNull(resp.licenseToken());
    assertEquals(LicenseAccessStatus.TOKEN_ACTIVE, resp.status());

    verify(licenseValidationService).validateLicense(req, token);
    verifyNoInteractions(clientIdGenerator, tokenManager, jwtBlacklistService);
    verifyNoMoreInteractions(licenseValidationService);
  }
}
