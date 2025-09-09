package io.github.bsayli.licensing.service.validation.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.config.ServiceCatalogProperties;
import io.github.bsayli.licensing.domain.model.LicenseChecksumVersionInfo;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseServiceIdVersionInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.exception.serviceid.LicenseInvalidChecksumException;
import io.github.bsayli.licensing.service.exception.serviceid.LicenseInvalidServiceIdException;
import io.github.bsayli.licensing.service.exception.serviceid.LicenseServiceIdNotSupportedException;
import io.github.bsayli.licensing.service.exception.version.LicenseServiceVersionNotSupportedException;
import io.github.bsayli.licensing.service.validation.ChecksumLookup;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseServicePolicyValidatorImpl")
class LicenseServicePolicyValidatorImplTest {

  private static LicenseInfo lic(
      Set<String> services,
      List<LicenseServiceIdVersionInfo> versions,
      Map<String, List<LicenseChecksumVersionInfo>> checksums) {
    return new LicenseInfo.Builder()
        .userId("user-1")
        .licenseTier("PRO")
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(7))
        .instanceIds(List.of("inst-1"))
        .maxCount(5)
        .remainingUsageCount(5)
        .allowedServices(services)
        .allowedServiceVersions(versions)
        .serviceChecksums(checksums)
        .build();
  }

  private static IssueTokenRequest issue(String id, String version, String checksum) {
    String instanceId = "instance-abcdefgh";
    String signature = "s".repeat(40);
    String licenseKey = "l".repeat(220);
    boolean force = false;
    return new IssueTokenRequest(id, version, instanceId, signature, checksum, licenseKey, force);
  }

  private static ValidateTokenRequest validateReq(String id, String version, String checksum) {
    String instanceId = "instance-abcdefgh";
    String signature = "s".repeat(40);
    return new ValidateTokenRequest(id, version, instanceId, signature, checksum);
  }

  private static LicenseServicePolicyValidatorImpl svc(
      Set<String> knownIds, Set<String> checksumRequired, ChecksumLookup lookup) {
    ServiceCatalogProperties catalog = new ServiceCatalogProperties(knownIds, checksumRequired);
    return new LicenseServicePolicyValidatorImpl(catalog, lookup);
  }

  @Test
  @DisplayName("happy path: known+licensed service, checksum matches, version <= max")
  void ok_all_good_issue() {
    String svcId = "svcA";
    String ver = "1.2.3";
    String sum = "c".repeat(32);

    var license =
        lic(
            Set.of(svcId),
            List.of(new LicenseServiceIdVersionInfo(svcId, "2.0.0")),
            Map.of(svcId, List.of(new LicenseChecksumVersionInfo(ver, sum))));

    var validator = svc(Set.of(svcId), Set.of(svcId), new ChecksumLookupImpl());
    var req = issue(svcId, ver, sum);

    assertDoesNotThrow(() -> validator.assertValid(license, req));
  }

  @Test
  @DisplayName("unknown service id -> LicenseInvalidServiceIdException")
  void unknown_service_id_validate() {
    String svcId = "svcA";
    var license = lic(Set.of("other"), List.of(), Map.of());

    var validator = svc(Set.of("other"), Set.of(), new ChecksumLookupImpl());
    var req = validateReq(svcId, "1.0.0", null);

    assertThrows(LicenseInvalidServiceIdException.class, () -> validator.assertValid(license, req));
  }

  @Test
  @DisplayName("not licensed service id -> LicenseServiceIdNotSupportedException")
  void not_licensed_service_id_issue() {
    String svcId = "svcA";
    var license = lic(Set.of("different"), List.of(), Map.of());

    var validator = svc(Set.of(svcId), Set.of(), new ChecksumLookupImpl());
    var req = issue(svcId, "1.0.0", null);

    assertThrows(
        LicenseServiceIdNotSupportedException.class, () -> validator.assertValid(license, req));
  }

  @Test
  @DisplayName("checksum required but mismatched -> LicenseInvalidChecksumException")
  void checksum_required_mismatch_validate() {
    String svcId = "svcA";
    String good = "good-sum";

    var license =
        lic(
            Set.of(svcId),
            List.of(),
            Map.of(svcId, List.of(new LicenseChecksumVersionInfo(good, "1.0.0"))));

    var validator = svc(Set.of(svcId), Set.of(svcId), new ChecksumLookupImpl());
    var req = validateReq(svcId, "1.0.0", "bad-sum");

    assertThrows(LicenseInvalidChecksumException.class, () -> validator.assertValid(license, req));
  }

  @Test
  @DisplayName("version above licensed max -> LicenseServiceVersionNotSupportedException")
  void version_above_max_issue() {
    String svcId = "svcA";
    var license =
        lic(Set.of(svcId), List.of(new LicenseServiceIdVersionInfo(svcId, "1.2.3")), Map.of());

    var validator = svc(Set.of(svcId), Set.of(), new ChecksumLookupImpl());
    var req = issue(svcId, "1.2.4", null);

    assertThrows(
        LicenseServiceVersionNotSupportedException.class,
        () -> validator.assertValid(license, req));
  }

  @Test
  @DisplayName("invalid version format -> LicenseServiceVersionNotSupportedException")
  void invalid_version_format_issue() {
    String svcId = "svcA";
    var license =
        lic(Set.of(svcId), List.of(new LicenseServiceIdVersionInfo(svcId, "9.9.9")), Map.of());

    var validator = svc(Set.of(svcId), Set.of(), new ChecksumLookupImpl());
    var req = issue(svcId, "1.2", null); // not x.y.z

    assertThrows(
        LicenseServiceVersionNotSupportedException.class,
        () -> validator.assertValid(license, req));
  }

  @Test
  @DisplayName("checksum-version mismatch -> LicenseServiceVersionNotSupportedException")
  void checksum_version_mismatch_validate() {
    String svcId = "svcA";
    String sum = "c".repeat(32);
    String checksumBoundVersion = "1.0.0";
    String requestedVersion = "2.0.0";

    var license =
        lic(
            Set.of(svcId),
            List.of(new LicenseServiceIdVersionInfo(svcId, "9.9.9")),
            Map.of(svcId, List.of(new LicenseChecksumVersionInfo(checksumBoundVersion, sum))));

    var validator = svc(Set.of(svcId), Set.of(svcId), new ChecksumLookupImpl());
    var req = validateReq(svcId, requestedVersion, sum);

    assertThrows(
        LicenseServiceVersionNotSupportedException.class,
        () -> validator.assertValid(license, req));
  }
}
