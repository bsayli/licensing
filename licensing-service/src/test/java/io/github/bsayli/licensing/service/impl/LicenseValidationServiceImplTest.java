package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.LicenseEvaluationService;
import io.github.bsayli.licensing.service.exception.token.TokenExpiredException;
import io.github.bsayli.licensing.service.validation.LicenseKeyRequestValidator;
import io.github.bsayli.licensing.service.validation.TokenRequestValidator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseValidationServiceImpl")
class LicenseValidationServiceImplTest {

  @Mock private LicenseEvaluationService licenseEvaluationService;
  @Mock private TokenRequestValidator tokenValidationService;
  @Mock private LicenseKeyRequestValidator licenseKeyValidationService;
  @Mock private UserIdEncryptor userIdEncryptor;

  @InjectMocks private LicenseValidationServiceImpl service;

  private LicenseInfo licenseInfo(String userId, String tier, LicenseStatus status) {
    return new LicenseInfo.Builder()
        .userId(userId)
        .licenseTier(tier)
        .licenseStatus(status)
        .expirationDate(LocalDateTime.now().plusDays(1))
        .maxCount(1)
        .remainingUsageCount(1)
        .build();
  }

  @Test
  @DisplayName(
      "validateLicense(IssueAccessRequest): validate signature → extract userId from licenseKey → cache check → evaluate → return ENCRYPTED user")
  void issueAccessFlow_shouldValidateAndReturnEncryptedUser() {
    var req =
        new IssueAccessRequest(
            "LICENSE_KEY", "instance-12345678", "chk-xxxxx", "crm", "1.2.3", "sig-xxxxx");

    when(userIdEncryptor.extractAndDecryptUserId("LICENSE_KEY")).thenReturn("user-123");
    var info = licenseInfo("user-123", "PRO", LicenseStatus.ACTIVE);
    when(licenseEvaluationService.evaluateLicense(req, "user-123")).thenReturn(info);
    when(userIdEncryptor.encrypt("user-123")).thenReturn("enc-user-123");

    LicenseValidationResult res = service.validateLicense(req);

    assertNotNull(res);
    assertTrue(res.valid());
    assertEquals("enc-user-123", res.userId());
    assertEquals("instance-12345678", res.appInstanceId());
    assertEquals("PRO", res.licenseTier());
    assertEquals(LicenseStatus.ACTIVE, res.licenseStatus());
    assertEquals("license.key.valid", res.message());

    verify(licenseKeyValidationService).assertSignatureValid(req);
    verify(userIdEncryptor).extractAndDecryptUserId("LICENSE_KEY");
    verify(licenseKeyValidationService).assertNoConflictingCachedContext(req, "user-123");
    verify(licenseEvaluationService).evaluateLicense(req, "user-123");
    verify(userIdEncryptor).encrypt("user-123");
    verifyNoMoreInteractions(
        licenseKeyValidationService,
        userIdEncryptor,
        licenseEvaluationService,
        tokenValidationService);
  }

  @Test
  @DisplayName(
      "validateLicense(ValidateAccessRequest): when token is valid → do not re-evaluate, return TOKEN_VALID")
  void validateTokenFlow_valid_noRefresh() {
    var req =
        new ValidateAccessRequest("instance-abcdef12", "chk-zzzz", "billing", "2.0.0", "sig-yyyy");
    String token = "jwt-current";

    doNothing().when(tokenValidationService).assertValid(req, token);

    LicenseValidationResult res = service.validateLicense(req, token);

    assertNotNull(res);
    assertTrue(res.valid());
    assertEquals("license.token.valid", res.message());
    assertNull(res.userId());
    assertNull(res.licenseTier());
    assertNull(res.licenseStatus());
    assertNull(res.serviceStatus());

    verify(tokenValidationService).assertValid(req, token);
    verifyNoInteractions(licenseEvaluationService, userIdEncryptor, licenseKeyValidationService);
  }

  @Test
  @DisplayName(
      "validateLicense(ValidateAccessRequest): when token is expired → evaluate with decrypted user and return TOKEN_REFRESHED")
  void validateTokenFlow_expired_triggersRefresh() {
    // NEW ORDER: instanceId, checksum, serviceId, serviceVersion, signature
    var req =
        new ValidateAccessRequest("instance-12345678", "chk-xxxxx", "crm", "1.2.3", "sig-xxxxx");
    String token = "jwt-expired";
    String encUserFromException = "enc-U-42";

    doThrow(new TokenExpiredException(encUserFromException))
        .when(tokenValidationService)
        .assertValid(req, token);

    when(userIdEncryptor.decrypt(encUserFromException)).thenReturn("user-42");
    var info = licenseInfo("user-42", "ENTERPRISE", LicenseStatus.ACTIVE);
    when(licenseEvaluationService.evaluateLicense(req, "user-42")).thenReturn(info);
    when(userIdEncryptor.encrypt("user-42")).thenReturn("enc-user-42");

    LicenseValidationResult res = service.validateLicense(req, token);

    assertNotNull(res);
    assertTrue(res.valid());
    assertEquals("enc-user-42", res.userId());
    assertEquals("instance-12345678", res.appInstanceId());
    assertEquals("ENTERPRISE", res.licenseTier());
    assertEquals(LicenseStatus.ACTIVE, res.licenseStatus());
    assertEquals(ServiceErrorCode.TOKEN_REFRESHED, res.serviceStatus());
    assertEquals("license.token.refreshed", res.message());

    verify(tokenValidationService).assertValid(req, token);
    verify(userIdEncryptor).decrypt(encUserFromException);
    verify(licenseEvaluationService).evaluateLicense(req, "user-42");
    verify(userIdEncryptor).encrypt("user-42");
    verifyNoMoreInteractions(tokenValidationService, userIdEncryptor, licenseEvaluationService);
    verifyNoInteractions(licenseKeyValidationService);
  }
}
