package io.github.bsayli.licensing.service.validation.impl;

import com.fasterxml.jackson.core.Version;
import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.config.ServiceCatalogProperties;
import io.github.bsayli.licensing.model.LicenseChecksumVersionInfo;
import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.model.LicenseServiceIdVersionInfo;
import io.github.bsayli.licensing.model.errors.InvalidServiceVersionFormatException;
import io.github.bsayli.licensing.model.errors.LicenseInvalidChecksumException;
import io.github.bsayli.licensing.model.errors.LicenseInvalidServiceIdException;
import io.github.bsayli.licensing.model.errors.LicenseServiceIdNotSupportedException;
import io.github.bsayli.licensing.model.errors.LicenseServiceVersionNotSupportedException;
import io.github.bsayli.licensing.service.validation.ChecksumLookup;
import io.github.bsayli.licensing.service.validation.LicenseServicePolicyValidator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LicenseServicePolicyValidatorImpl implements LicenseServicePolicyValidator {

  private final ServiceCatalogProperties catalog;
  private final ChecksumLookup checksumLookup;

  public LicenseServicePolicyValidatorImpl(
      ServiceCatalogProperties catalog, ChecksumLookup checksumLookup) {
    this.catalog = catalog;
    this.checksumLookup = checksumLookup;
  }

  @Override
  public void assertValid(LicenseInfo licenseInfo, IssueTokenRequest request) {
    validateServiceId(licenseInfo, request.serviceId());
    validateChecksum(licenseInfo, request.serviceId(), request.checksum());
    validateVersion(licenseInfo, request.serviceId(), request.serviceVersion(), request.checksum());
  }

  @Override
  public void assertValid(LicenseInfo licenseInfo, ValidateTokenRequest request) {
    validateServiceId(licenseInfo, request.serviceId());
    validateChecksum(licenseInfo, request.serviceId(), request.checksum());
    validateVersion(licenseInfo, request.serviceId(), request.serviceVersion(), request.checksum());
  }

  private void validateServiceId(LicenseInfo licenseInfo, String requestedServiceId) {
    if (!isServiceIdKnown(requestedServiceId)) {
      throw new LicenseInvalidServiceIdException(requestedServiceId);
    }
    if (!isServiceIdLicensed(requestedServiceId, licenseInfo.allowedServices())) {
      throw new LicenseServiceIdNotSupportedException(requestedServiceId);
    }
  }

  private boolean isServiceIdKnown(String serviceId) {
    return catalog.ids() != null && catalog.ids().contains(serviceId);
  }

  private boolean isServiceIdLicensed(String serviceId, List<String> allowedServices) {
    return !CollectionUtils.isEmpty(allowedServices) && allowedServices.contains(serviceId);
  }

  private void validateChecksum(LicenseInfo info, String serviceId, String requestedChecksum) {
    if (!isChecksumValid(info, serviceId, requestedChecksum)) {
      throw new LicenseInvalidChecksumException();
    }
  }

  private boolean isChecksumValid(LicenseInfo info, String serviceId, String requestedChecksum) {
    boolean required = isChecksumRequired(serviceId) && requestedChecksum != null;
    if (!required) return true;

    return checksumLookup.checksumsFor(info, serviceId).stream()
        .anyMatch(c -> Objects.equals(c.checksum(), requestedChecksum));
  }

  private boolean isChecksumRequired(String serviceId) {
    return catalog.checksumRequired() != null && catalog.checksumRequired().contains(serviceId);
  }

  private void validateVersion(
      LicenseInfo info, String serviceId, String requestedVersion, String requestedChecksum) {

    if (!isVersionWithinLicensedMax(info, serviceId, requestedVersion)) {
      throw new LicenseServiceVersionNotSupportedException(serviceId);
    }
    if (!isVersionCompatibleWithChecksum(info, serviceId, requestedVersion, requestedChecksum)) {
      throw new LicenseServiceVersionNotSupportedException(serviceId);
    }
  }

  private boolean isVersionWithinLicensedMax(
      LicenseInfo info, String serviceId, String requestedVersion) {
    List<LicenseServiceIdVersionInfo> allowed = info.allowedServiceVersions();
    if (CollectionUtils.isEmpty(allowed)) return true;

    Optional<String> maxOpt =
        allowed.stream()
            .filter(s -> serviceId.equals(s.serviceId()))
            .map(LicenseServiceIdVersionInfo::licensedMaxVersion)
            .findFirst();

    if (maxOpt.isEmpty()) return true;

    int[] max = parseSemanticVersion(maxOpt.get());
    int[] req = parseSemanticVersionSafe(requestedVersion, serviceId);

    Version vMax = new Version(max[0], max[1], max[2], null, null, null);
    Version vReq = new Version(req[0], req[1], req[2], null, null, null);
    return vReq.compareTo(vMax) <= 0;
  }

  private boolean isVersionCompatibleWithChecksum(
      LicenseInfo info, String serviceId, String requestedVersion, String requestedChecksum) {
    if (requestedChecksum == null) return true;
    return checksumLookup.checksumsFor(info, serviceId).stream()
        .filter(c -> Objects.equals(c.checksum(), requestedChecksum))
        .map(LicenseChecksumVersionInfo::version)
        .findFirst()
        .map(requestedVersion::equals)
        .orElse(true);
  }

  private int[] parseSemanticVersionSafe(String version, String serviceId) {
    try {
      return parseSemanticVersion(version);
    } catch (RuntimeException ex) {
      throw new LicenseServiceVersionNotSupportedException(serviceId);
    }
  }

  private int[] parseSemanticVersion(String version) {
    String[] parts = version.split("\\.");
    if (parts.length != 3) {
      throw new InvalidServiceVersionFormatException(version);
    }
    try {
      return new int[] {
        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])
      };
    } catch (NumberFormatException e) {
      throw new InvalidServiceVersionFormatException(version, e);
    }
  }
}
