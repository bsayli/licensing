package io.github.bsayli.licensing.model;

public enum LicenseStatus {
  ACTIVE,
  INACTIVE,
  EXPIRED;

  public static LicenseStatus from(String value) {
    for (LicenseStatus status : values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown license status: " + value);
  }

  public boolean isActive() {
    return this == ACTIVE;
  }
}
