package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.exception.license.LicenseNotFoundException;
import io.github.bsayli.licensing.service.user.orchestration.UserOrchestrationService;
import io.github.bsayli.licensing.service.validation.LicensePolicyValidator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseEvaluationServiceImpl")
class LicenseEvaluationServiceImplTest {

  @Mock private UserOrchestrationService userService;
  @Mock private LicensePolicyValidator policyValidator;

  @InjectMocks private LicenseEvaluationServiceImpl service;

  private LicenseInfo licenseInfoWithInstances(
      String userId, String tier, LicenseStatus status, List<String> instanceIds) {
    return new LicenseInfo.Builder()
        .userId(userId)
        .licenseTier(tier)
        .licenseStatus(status)
        .expirationDate(LocalDateTime.now().plusDays(1))
        .instanceIds(instanceIds)
        .maxCount(5)
        .remainingUsageCount(5)
        .build();
  }

  @Test
  @DisplayName("evaluateLicense(IssueAccessRequest): records usage when instance is missing")
  void issueToken_recordsUsage_whenMissing() {
    var request = new IssueAccessRequest("LK", "inst-001", "chk", "crm", "1.2.3", "sig", false);
    var info = licenseInfoWithInstances("user-1", "PRO", LicenseStatus.ACTIVE, List.of("inst-XYZ"));

    when(userService.getLicenseInfo("user-1")).thenReturn(info);
    doNothing().when(policyValidator).assertValid(info, request);
    when(policyValidator.isInstanceIdMissing("inst-001", info.instanceIds())).thenReturn(true);

    LicenseInfo out = service.evaluateLicense(request, "user-1");

    assertSame(info, out);
    verify(userService).getLicenseInfo("user-1");
    verify(policyValidator).assertValid(info, request);
    verify(policyValidator).isInstanceIdMissing("inst-001", info.instanceIds());
    verify(userService).recordUsage("user-1", "inst-001");
    verifyNoMoreInteractions(userService, policyValidator);
  }

  @Test
  @DisplayName("evaluateLicense(IssueAccessRequest): does not record usage when instance exists")
  void issueToken_doesNotRecord_whenExists() {
    var request = new IssueAccessRequest("LK", "inst-XYZ", "chk", "crm", "1.2.3", "sig", false);
    var info =
        licenseInfoWithInstances("user-2", "BASIC", LicenseStatus.ACTIVE, List.of("inst-XYZ"));

    when(userService.getLicenseInfo("user-2")).thenReturn(info);
    doNothing().when(policyValidator).assertValid(info, request);
    when(policyValidator.isInstanceIdMissing("inst-XYZ", info.instanceIds())).thenReturn(false);

    LicenseInfo out = service.evaluateLicense(request, "user-2");

    assertSame(info, out);
    verify(userService).getLicenseInfo("user-2");
    verify(policyValidator).assertValid(info, request);
    verify(policyValidator).isInstanceIdMissing("inst-XYZ", info.instanceIds());
    verify(userService, never()).recordUsage(anyString(), anyString());
    verifyNoMoreInteractions(userService, policyValidator);
  }

  @Test
  @DisplayName("evaluateLicense(ValidateAccessRequest): records usage when instance is missing")
  void validateToken_recordsUsage_whenMissing() {
    var request = new ValidateAccessRequest("inst-ABC", "chk", "billing", "2.0.0", "sig");
    var info = licenseInfoWithInstances("user-3", "ENTERPRISE", LicenseStatus.ACTIVE, List.of());

    when(userService.getLicenseInfo("user-3")).thenReturn(info);
    doNothing().when(policyValidator).assertValid(info, request);
    when(policyValidator.isInstanceIdMissing("inst-ABC", info.instanceIds())).thenReturn(true);

    LicenseInfo out = service.evaluateLicense(request, "user-3");

    assertSame(info, out);
    verify(userService).getLicenseInfo("user-3");
    verify(policyValidator).assertValid(info, request);
    verify(policyValidator).isInstanceIdMissing("inst-ABC", info.instanceIds());
    verify(userService).recordUsage("user-3", "inst-ABC");
    verifyNoMoreInteractions(userService, policyValidator);
  }

  @Test
  @DisplayName("evaluateLicense: throws LicenseNotFoundException when user is missing")
  void evaluate_throws_whenUserMissing() {
    var request = new IssueAccessRequest("LK", "inst-001", "chk", "crm", "1.2.3", "sig", false);
    when(userService.getLicenseInfo("missing-user"))
        .thenThrow(new LicenseNotFoundException("missing-user"));

    assertThrows(
        LicenseNotFoundException.class, () -> service.evaluateLicense(request, "missing-user"));

    verify(userService).getLicenseInfo("missing-user");
    verifyNoInteractions(policyValidator);
    verifyNoMoreInteractions(userService);
  }
}
