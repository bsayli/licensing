package io.github.bsayli.licensing.domain.model;

import java.time.LocalDateTime;
import java.util.*;

public record LicenseInfo(
    String userId,
    String licenseTier,
    LicenseStatus licenseStatus,
    LocalDateTime expirationDate,
    List<String> instanceIds,
    int maxCount,
    int remainingUsageCount,
    Set<String> allowedServices,
    List<LicenseServiceIdVersionInfo> allowedServiceVersions,
    Map<String, List<LicenseChecksumVersionInfo>> serviceChecksums) {

  public static class Builder {
    private final List<String> instanceIds = new ArrayList<>();
    private final Set<String> allowedServices = new HashSet<>();
    private final List<LicenseServiceIdVersionInfo> allowedServiceVersions = new ArrayList<>();
    private final Map<String, List<LicenseChecksumVersionInfo>> serviceChecksums = new HashMap<>();
    private String userId;
    private String licenseTier;
    private LicenseStatus licenseStatus;
    private LocalDateTime expirationDate;
    private int maxCount;
    private int remainingUsageCount;

    private static Map<String, List<LicenseChecksumVersionInfo>> copyDeep(
        Map<String, List<LicenseChecksumVersionInfo>> src) {
      Map<String, List<LicenseChecksumVersionInfo>> out = new HashMap<>();
      src.forEach((k, v) -> out.put(k, (v == null) ? List.of() : List.copyOf(v)));
      return Map.copyOf(out);
    }

    public Builder userId(String v) {
      this.userId = v;
      return this;
    }

    public Builder licenseTier(String v) {
      this.licenseTier = v;
      return this;
    }

    public Builder licenseStatus(LicenseStatus v) {
      this.licenseStatus = v;
      return this;
    }

    public Builder expirationDate(LocalDateTime v) {
      this.expirationDate = v;
      return this;
    }

    public Builder instanceIds(List<String> v) {
      if (v != null) {
        this.instanceIds.addAll(v);
      }
      return this;
    }

    public Builder maxCount(int v) {
      this.maxCount = v;
      return this;
    }

    public Builder remainingUsageCount(int v) {
      this.remainingUsageCount = v;
      return this;
    }

    public Builder allowedServices(Collection<String> v) {
      if (v != null) {
        this.allowedServices.addAll(v);
      }
      return this;
    }

    public Builder allowedServiceVersions(List<LicenseServiceIdVersionInfo> v) {
      if (v != null) {
        this.allowedServiceVersions.addAll(v);
      }
      return this;
    }

    public Builder putServiceChecksums(
        String serviceId, List<LicenseChecksumVersionInfo> checksums) {
      if (serviceId != null && checksums != null) {
        this.serviceChecksums.put(serviceId, List.copyOf(checksums));
      }
      return this;
    }

    public Builder serviceChecksums(Map<String, List<LicenseChecksumVersionInfo>> m) {
      if (m != null) {
        m.forEach(this::putServiceChecksums);
      }
      return this;
    }

    public LicenseInfo build() {
      if (userId == null
          || licenseTier == null
          || licenseStatus == null
          || expirationDate == null
          || maxCount == 0) {
        throw new IllegalArgumentException("Required parameters are not set.");
      }

      return new LicenseInfo(
          userId,
          licenseTier,
          licenseStatus,
          expirationDate,
          List.copyOf(instanceIds),
          maxCount,
          remainingUsageCount,
          Set.copyOf(allowedServices),
          List.copyOf(allowedServiceVersions),
          copyDeep(serviceChecksums));
    }
  }
}
