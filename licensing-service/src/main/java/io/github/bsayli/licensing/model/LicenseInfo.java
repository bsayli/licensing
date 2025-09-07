package io.github.bsayli.licensing.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record LicenseInfo(
        String userId,
        String licenseTier,
        String licenseStatus,
        LocalDateTime expirationDate,
        List<String> instanceIds,
        int maxCount,
        int remainingUsageCount,
        List<String> allowedServices,
        List<LicenseServiceIdVersionInfo> allowedServiceVersions,
        List<LicenseChecksumVersionInfo> checksumsCrm,
        List<LicenseChecksumVersionInfo> checksumsBilling,
        List<LicenseChecksumVersionInfo> checksumsReporting) {

  public static class Builder {
    private final List<String> instanceIds = new ArrayList<>();
    private final List<String> allowedServices = new ArrayList<>();
    private final List<LicenseServiceIdVersionInfo> allowedServiceVersions = new ArrayList<>();
    private final List<LicenseChecksumVersionInfo> checksumsCrm = new ArrayList<>();
    private final List<LicenseChecksumVersionInfo> checksumsBilling = new ArrayList<>();
    private final List<LicenseChecksumVersionInfo> checksumsReporting = new ArrayList<>();
    private String userId;
    private String licenseTier;
    private String licenseStatus;
    private LocalDateTime expirationDate;
    private int maxCount;
    private int remainingUsageCount;

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder licenseTier(String licenseTier) {
      this.licenseTier = licenseTier;
      return this;
    }

    public Builder licenseStatus(String licenseStatus) {
      this.licenseStatus = licenseStatus;
      return this;
    }

    public Builder expirationDate(LocalDateTime expirationDate) {
      this.expirationDate = expirationDate;
      return this;
    }

    public Builder instanceIds(List<String> instanceIds) {
      if (instanceIds != null) this.instanceIds.addAll(instanceIds);
      return this;
    }

    public Builder maxCount(int maxCount) {
      this.maxCount = maxCount;
      return this;
    }

    public Builder remainingUsageCount(int remainingUsageCount) {
      this.remainingUsageCount = remainingUsageCount;
      return this;
    }

    public Builder allowedServices(List<String> allowedServices) {
      if (allowedServices != null) this.allowedServices.addAll(allowedServices);
      return this;
    }

    public Builder allowedServiceVersions(List<LicenseServiceIdVersionInfo> allowedServiceVersions) {
      if (allowedServiceVersions != null) this.allowedServiceVersions.addAll(allowedServiceVersions);
      return this;
    }

    public Builder checksumsCrm(List<LicenseChecksumVersionInfo> checksumsCrm) {
      if (checksumsCrm != null) this.checksumsCrm.addAll(checksumsCrm);
      return this;
    }

    public Builder checksumsBilling(List<LicenseChecksumVersionInfo> checksumsBilling) {
      if (checksumsBilling != null) this.checksumsBilling.addAll(checksumsBilling);
      return this;
    }

    public Builder checksumsReporting(List<LicenseChecksumVersionInfo> checksumsReporting) {
      if (checksumsReporting != null) this.checksumsReporting.addAll(checksumsReporting);
      return this;
    }

    public LicenseInfo build() {
      if (userId == null || licenseTier == null || licenseStatus == null || expirationDate == null || maxCount == 0) {
        throw new IllegalArgumentException("Required parameters are not set.");
      }
      return new LicenseInfo(
              userId,
              licenseTier,
              licenseStatus,
              expirationDate,
              instanceIds,
              maxCount,
              remainingUsageCount,
              allowedServices,
              allowedServiceVersions,
              checksumsCrm,
              checksumsBilling,
              checksumsReporting
      );
    }
  }
}