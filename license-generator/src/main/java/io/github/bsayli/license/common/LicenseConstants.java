package io.github.bsayli.license.common;

public final class LicenseConstants {

  public static final String LICENSE_KEY_PREFIX = "BSAYLI";
  public static final String LICENSE_DELIMITER = ".";
  public static final int RANDOM_BYTES_FOR_KEY = 32;

  public static final String CLAIM_LICENSE_STATUS = "licenseStatus";
  public static final String CLAIM_LICENSE_TIER = "licenseTier";
  public static final String CLAIM_MESSAGE = "message";
  public static final String CLAIM_EXPIRATION = "exp";
  public static final String CLAIM_ISSUED_AT = "iat";
  public static final String CLAIM_SUBJECT = "sub";

  private LicenseConstants() {}
}
