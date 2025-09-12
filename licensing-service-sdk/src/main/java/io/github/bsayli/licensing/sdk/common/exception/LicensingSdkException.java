package io.github.bsayli.licensing.sdk.common.exception;

public abstract class LicensingSdkException extends RuntimeException {
  protected LicensingSdkException(String message) {
    super(message);
  }

  protected LicensingSdkException(String message, Throwable cause) {
    super(message, cause);
  }
}
