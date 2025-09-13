package io.github.bsayli.licensing.service.user.exception;

public enum UserOperationErrorCode {
  ALREADY_PROCESSING("user.async.already.processing"),
  MAX_RETRY_ATTEMPTS_EXCEEDED("user.async.max.retry.exceeded");

  private final String messageKey;

  UserOperationErrorCode(String messageKey) {
    this.messageKey = messageKey;
  }

  public String messageKey() {
    return messageKey;
  }
}
