package io.github.bsayli.licensing.model.errors;

public class TokenExpiredException extends LicenseServiceExceptionImpl {

  private final String encUserId;

  public TokenExpiredException(String encUserId, Object... args) {
    super(LicenseServiceStatus.TOKEN_EXPIRED, args);
    this.encUserId = encUserId;
  }

  public TokenExpiredException(String encUserId, Throwable cause, Object... args) {
    super(LicenseServiceStatus.TOKEN_EXPIRED, cause, args);
    this.encUserId = encUserId;
  }

  public String getEncUserId() {
    return encUserId;
  }
}
