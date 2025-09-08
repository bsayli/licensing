package io.github.bsayli.licensing.domain.model;

public enum LicenseStatus {
  ACTIVE("Active"),
  INACTIVE("Inactive"),
  TRIAL("Trial"),
  SUSPENDED("Suspended"),
  GRACE_PERIOD("Grace Period"),
  EXPIRED("Expired"),
  REVOKED("Revoked"),
  PENDING_ACTIVATION("Pending Activation");

  private final String displayName;

  LicenseStatus(String displayName) {
    this.displayName = displayName;
  }

  public static LicenseStatus from(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("License status cannot be null or empty");
    }
    String normalized = value.trim();
    for (LicenseStatus s : values()) {
      if (s.name().equalsIgnoreCase(normalized.replace(" ", "_"))
          || s.displayName.equalsIgnoreCase(normalized)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown license status: " + value);
  }

  public String displayName() {
    return displayName;
  }

  public boolean isActive() {
    return this == ACTIVE || this == TRIAL || this == GRACE_PERIOD;
  }
}
