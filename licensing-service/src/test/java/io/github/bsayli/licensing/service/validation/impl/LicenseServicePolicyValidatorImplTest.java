package io.github.bsayli.licensing.service.validation.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
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

@Tag("unit")
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
        .instanceIds(List.of("instance-abcdefgh"))
        .maxCount(5)
        .remainingUsageCount(5)
        .allowedServices(services)
        .allowedServiceVersions(versions)
        .serviceChecksums(checksums)
        .build();
  }

  // NEW order: licenseKey, instanceId, checksum, serviceId, serviceVersion, signature,
  // forceTokenRefresh
  private static IssueAccessRequest issue(String serviceId, String version, String checksum) {
    String licenseKey = "L".repeat(120); // >=100
    String instanceId = "instance-abcdefgh"; // >=10
    String signature = "S".repeat(80); // >=60
    return new IssueAccessRequest(licenseKey, instanceId, checksum, serviceId, version, signature);
  }

  // NEW order: instanceId, checksum, serviceId, serviceVersion, signature
  private static ValidateAccessRequest validateReq(
      String serviceId, String version, String checksum) {
    String instanceId = "instance-abcdefgh"; // >=10
    String signature = "S".repeat(80); // >=60
    return new ValidateAccessRequest(instanceId, checksum, serviceId, version, signature);
  }

  private static LicenseServicePolicyValidatorImpl svc(
      Set<String> knownIds, Set<String> checksumRequired, ChecksumLookup lookup) {
    ServiceCatalogProperties catalog = new ServiceCatalogProperties(knownIds, checksumRequired);
    return new LicenseServicePolicyValidatorImpl(catalog, lookup);
  }

  @Test
  @DisplayName("happy path: known + licensed service, checksum matches, version <= max")
  void ok_all_good_issue() {
    String svcId = "svcA";
    String ver = "1.2.3";
    String sum = "c".repeat(40); // checksum must be >= 40

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
    String goodChecksum = "g".repeat(40);

    var license =
        lic(
            Set.of(svcId),
            List.of(),
            Map.of(svcId, List.of(new LicenseChecksumVersionInfo("1.0.0", goodChecksum))));

    var validator = svc(Set.of(svcId), Set.of(svcId), new ChecksumLookupImpl());
    var req =
        validateReq(svcId, "1.0.0", "bad-sum".repeat(4)); // >=40, but not equal to goodChecksum

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
    String sum = "c".repeat(40);
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

  /** Minimal in-test implementation so we don't rely on other test helpers. */
  private static final class ChecksumLookupImpl implements ChecksumLookup {
    @Override
    public List<LicenseChecksumVersionInfo> checksumsFor(LicenseInfo info, String serviceId) {
      Map<String, List<LicenseChecksumVersionInfo>> map = info.serviceChecksums();
      return map == null ? List.of() : map.getOrDefault(serviceId, List.of());
    }
  }
}
