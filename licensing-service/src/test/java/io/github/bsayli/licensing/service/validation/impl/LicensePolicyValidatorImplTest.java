package io.github.bsayli.licensing.service.validation.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseServiceIdVersionInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.exception.license.LicenseExpiredException;
import io.github.bsayli.licensing.service.exception.license.LicenseInactiveException;
import io.github.bsayli.licensing.service.exception.license.LicenseUsageLimitExceededException;
import io.github.bsayli.licensing.service.validation.LicenseServicePolicyValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicensePolicyValidatorImpl")
class LicensePolicyValidatorImplTest {

  @Mock private LicenseServicePolicyValidator detailValidator;

  private LicensePolicyValidatorImpl validator;

  private static LicenseInfo lic(
      LicenseStatus status,
      LocalDateTime expiration,
      int maxCount,
      int remaining,
      List<String> instanceIds) {
    return new LicenseInfo.Builder()
        .userId("u1")
        .licenseTier("PRO")
        .licenseStatus(status)
        .expirationDate(expiration)
        .instanceIds(instanceIds)
        .maxCount(maxCount)
        .remainingUsageCount(remaining)
        .allowedServices(Set.of("svcA"))
        .allowedServiceVersions(List.of(new LicenseServiceIdVersionInfo("svcA", "9.9.9")))
        .serviceChecksums(Map.of())
        .build();
  }

  /** Build IssueAccessRequest with the NEW parameter order. */
  private static IssueAccessRequest issueReq(String instanceId) {
    return new IssueAccessRequest(
        "L".repeat(120), // licenseKey (100..200) — dummy
        instanceId, // instanceId (>=10)
        "C".repeat(40), // checksum (>=40)
        "svcA", // serviceId
        "1.0.0", // serviceVersion
        "S".repeat(80) // signature (>=60)
        );
  }

  /** Build ValidateAccessRequest with the NEW parameter order. */
  private static ValidateAccessRequest validateReq(String instanceId) {
    return new ValidateAccessRequest(
        instanceId, // instanceId (>=10)
        "C".repeat(40), // checksum (>=40)
        "svcA", // serviceId
        "1.0.0", // serviceVersion
        "S".repeat(80) // signature (>=60)
        );
  }

  @BeforeEach
  void setUp() {
    validator = new LicensePolicyValidatorImpl(detailValidator);
  }

  @Test
  @DisplayName("assertValid(Issue): happy path -> delegates to detail validator")
  void issue_happyPath_delegates() {
    var license =
        lic(
            LicenseStatus.ACTIVE,
            LocalDateTime.now().plusDays(1),
            5,
            5,
            List.of("instance-1", "instance-2"));
    var req = issueReq("instance-1");

    assertDoesNotThrow(() -> validator.assertValid(license, req));
    verify(detailValidator, times(1)).assertValid(license, req);
    verifyNoMoreInteractions(detailValidator);
  }

  @Test
  @DisplayName("assertValid(Validate): happy path -> delegates to detail validator")
  void validate_happyPath_delegates() {
    var license =
        lic(LicenseStatus.ACTIVE, LocalDateTime.now().plusDays(1), 3, 1, List.of("instance-x"));
    var req = validateReq("instance-x");

    assertDoesNotThrow(() -> validator.assertValid(license, req));
    verify(detailValidator, times(1)).assertValid(license, req);
    verifyNoMoreInteractions(detailValidator);
  }

  @Test
  @DisplayName("expired license -> LicenseExpiredException (before detail validation)")
  void expired_throws() {
    var license =
        lic(LicenseStatus.ACTIVE, LocalDateTime.now().minusMinutes(1), 5, 5, List.of("instance-1"));
    var req = issueReq("instance-1");

    assertThrows(LicenseExpiredException.class, () -> validator.assertValid(license, req));
    verifyNoInteractions(detailValidator);
  }

  @Test
  @DisplayName("inactive license -> LicenseInactiveException (before detail validation)")
  void inactive_throws() {
    var license =
        lic(LicenseStatus.INACTIVE, LocalDateTime.now().plusDays(1), 5, 5, List.of("instance-1"));
    var req = validateReq("instance-1");

    assertThrows(LicenseInactiveException.class, () -> validator.assertValid(license, req));
    verifyNoInteractions(detailValidator);
  }

  @Test
  @DisplayName("usage exceeded + new instance -> LicenseUsageLimitExceededException")
  void usageExceeded_newInstance_throws() {
    var license =
        lic(
            LicenseStatus.ACTIVE,
            LocalDateTime.now().plusDays(1),
            2,
            0, // no remaining
            List.of("instance-1")); // does not include the new one
    var req = issueReq("instance-NEW");

    assertThrows(
        LicenseUsageLimitExceededException.class, () -> validator.assertValid(license, req));
    verifyNoInteractions(detailValidator);
  }

  @Test
  @DisplayName("usage exceeded + existing instance -> allowed (no throw), then delegate")
  void usageExceeded_existingInstance_ok() {
    var license =
        lic(
            LicenseStatus.ACTIVE,
            LocalDateTime.now().plusDays(1),
            2,
            0, // exhausted quota
            List.of("instance-1", "instance-2")); // existing includes requested
    var req = validateReq("instance-2");

    assertDoesNotThrow(() -> validator.assertValid(license, req));
    verify(detailValidator, times(1)).assertValid(license, req);
    verifyNoMoreInteractions(detailValidator);
  }

  @Test
  @DisplayName("isInstanceIdMissing utility")
  void isInstanceIdMissing_checks() {
    assertTrue(validator.isInstanceIdMissing("x", null));
    assertTrue(validator.isInstanceIdMissing("x", List.of()));
    assertTrue(validator.isInstanceIdMissing("x", List.of("a", "b")));
    assertFalse(validator.isInstanceIdMissing("x", List.of("a", "x", "b")));
  }
}
