package io.github.bsayli.licensing.model.errors;

public class TokenIsTooOldForRefreshException extends LicenseServiceExceptionImpl {

  public TokenIsTooOldForRefreshException(Object... args) {
    super(LicenseServiceStatus.TOKEN_IS_TOO_OLD_FOR_REFRESH, args);
  }

  public TokenIsTooOldForRefreshException(Throwable cause, Object... args) {
    super(LicenseServiceStatus.TOKEN_IS_TOO_OLD_FOR_REFRESH, cause, args);
  }
}
