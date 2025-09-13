package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.LicenseAccessStatus;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.LicenseValidationService;
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
  @Mock private LicenseTokenManager tokenManager;

  @InjectMocks private LicenseOrchestrationServiceImpl service;

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
    verifyNoInteractions(clientIdGenerator, tokenManager);
    verifyNoMoreInteractions(licenseValidationService);
  }

  @Test
  @DisplayName(
      "issueAccess: active token exists -> return ACTIVE(existing) without issuing new token")
  void issueAccess_activeExists() {
    var req =
        new io.github.bsayli.licensing.api.dto.IssueAccessRequest(
            "L".repeat(100) + "~rnd~" + "A".repeat(64), // licenseKey
            "instance-12345678",
            "chk-xxxxx",
            "crm",
            "1.2.3",
            "sig-xxxxx");

    var validationResult =
        new LicenseValidationResult.Builder()
            .valid(true)
            .userId("enc-user-1")
            .appInstanceId("instance-12345678")
            .licenseStatus(LicenseStatus.ACTIVE)
            .licenseTier("PRO")
            .message("ok")
            .build();

    when(licenseValidationService.validateLicense(req)).thenReturn(validationResult);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-123");
    when(tokenManager.peekActive("client-123")).thenReturn("jwt-existing");

    LicenseAccessResponse resp = service.issueAccess(req);

    assertNotNull(resp);
    assertEquals(LicenseAccessStatus.TOKEN_ACTIVE, resp.status());
    assertEquals("jwt-existing", resp.licenseToken());

    verify(licenseValidationService).validateLicense(req);
    verify(clientIdGenerator).getClientId(req);
    verify(tokenManager).peekActive("client-123");
    verifyNoMoreInteractions(licenseValidationService, clientIdGenerator, tokenManager);
  }

  @Test
  @DisplayName("issueAccess: no active token -> issue & cache -> return CREATED(newToken)")
  void issueAccess_issueNew() {
    var req =
        new io.github.bsayli.licensing.api.dto.IssueAccessRequest(
            "L".repeat(100) + "~rnd~" + "A".repeat(64),
            "instance-abcdef12",
            "chk-zzzzz",
            "billing",
            "2.0.0",
            "sig-yyyyy");

    var validationResult =
        new LicenseValidationResult.Builder()
            .valid(true)
            .userId("enc-user-2")
            .appInstanceId("instance-abcdef12")
            .licenseStatus(LicenseStatus.ACTIVE)
            .licenseTier("BASIC")
            .message("ok")
            .build();

    when(licenseValidationService.validateLicense(req)).thenReturn(validationResult);
    when(clientIdGenerator.getClientId(req)).thenReturn("client-999");
    when(tokenManager.peekActive("client-999")).thenReturn(null);
    when(tokenManager.issueAndCache(any(LicenseTokenIssueRequest.class))).thenReturn("jwt-new");

    LicenseAccessResponse resp = service.issueAccess(req);

    assertNotNull(resp);
    assertEquals(LicenseAccessStatus.TOKEN_CREATED, resp.status());
    assertEquals("jwt-new", resp.licenseToken());

    ArgumentCaptor<LicenseTokenIssueRequest> cap =
        ArgumentCaptor.forClass(LicenseTokenIssueRequest.class);
    verify(tokenManager).issueAndCache(cap.capture());
    var cmd = cap.getValue();
    assertEquals("client-999", cmd.clientId());
    assertEquals("billing", cmd.serviceId());
    assertEquals("2.0.0", cmd.serviceVersion());
    assertEquals("instance-abcdef12", cmd.instanceId());
    assertEquals("chk-zzzzz", cmd.checksum());
    assertEquals("sig-yyyyy", cmd.signature());

    verify(licenseValidationService).validateLicense(req);
    verify(clientIdGenerator).getClientId(req);
    verify(tokenManager).peekActive("client-999");
    verifyNoMoreInteractions(licenseValidationService, clientIdGenerator, tokenManager);
  }
}
