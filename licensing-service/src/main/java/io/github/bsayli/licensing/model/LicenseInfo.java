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
    List<LicenseChecksumVersionInfo> checksumsCodegen,
    List<LicenseChecksumVersionInfo> checksumsTestAutomation) {

  public static class Builder {
    private String userId;
    private String licenseTier;
    private String licenseStatus;
    private LocalDateTime expirationDate;
    private List<String> instanceIds = new ArrayList<>();
    private int maxCount;
    private int remainingUsageCount;
    private List<String> allowedServices = new ArrayList<>();
    private List<LicenseServiceIdVersionInfo> allowedServiceVersions = new ArrayList<>();
    private List<LicenseChecksumVersionInfo> checksumsCodegen = new ArrayList<>();
    private List<LicenseChecksumVersionInfo> checksumsTestAutomation = new ArrayList<>();

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
      this.instanceIds.addAll(instanceIds);
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
      this.allowedServices.addAll(allowedServices);
      return this;
    }

    public Builder allowedServiceVersions(
        List<LicenseServiceIdVersionInfo> allowedServiceVersions) {
      this.allowedServiceVersions.addAll(allowedServiceVersions);
      return this;
    }

    public Builder checksumsCodegen(List<LicenseChecksumVersionInfo> checksumsCodegen) {
      this.checksumsCodegen.addAll(checksumsCodegen);
      return this;
    }

    public Builder checksumsTestAutomation(
        List<LicenseChecksumVersionInfo> checksumsTestAutomation) {
      this.checksumsTestAutomation.addAll(checksumsTestAutomation);
      return this;
    }

    public LicenseInfo build() {
      if (userId == null
          || licenseTier == null
          || expirationDate == null
          || maxCount == 0
          || licenseStatus == null) {
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
          checksumsCodegen,
          checksumsTestAutomation);
    }
  }
}
