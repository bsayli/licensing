package io.github.bsayli.license.common;

/**
 * Domain-level constants for license formatting and JWT claim keys.
 *
 * <p>All "magic values" related to license encoding/decoding and claims are centralized here to
 * avoid duplication across modules.
 */
public final class LicenseConstants {

  /** Prefix prepended to all license keys (distinguishes vendor/domain). */
  public static final String LICENSE_KEY_PREFIX = "BSAYLI";

  /* ==============================
   * License key format
   * ============================== */
  /** Delimiter between license key segments (prefix ~ random ~ encryptedUserId). */
  public static final String LICENSE_DELIMITER = "~";

  /** Number of random bytes used for the license key middle segment. */
  public static final int RANDOM_BYTES_FOR_KEY = 32;

  /** Claim: overall license status (e.g., Active, Revoked, Expired). */
  public static final String CLAIM_LICENSE_STATUS = "licenseStatus";

  /* ==============================
   * Custom JWT claim keys (license domain)
   * ============================== */
  /** Claim: license tier or edition (e.g., Free, Pro, Enterprise). */
  public static final String CLAIM_LICENSE_TIER = "licenseTier";

  /** Claim: optional message attached to the license (e.g., errors, info). */
  public static final String CLAIM_MESSAGE = "message";

  /** JWT claim: expiration time (epoch seconds). */
  public static final String CLAIM_EXPIRATION = "exp";

  /* ==============================
   * Standard JWT claim keys
   * ============================== */
  /** JWT claim: issued-at timestamp (epoch seconds). */
  public static final String CLAIM_ISSUED_AT = "iat";

  /** JWT claim: subject (usually the principal/user id). */
  public static final String CLAIM_SUBJECT = "sub";

  private LicenseConstants() {
    // utility
  }
}
