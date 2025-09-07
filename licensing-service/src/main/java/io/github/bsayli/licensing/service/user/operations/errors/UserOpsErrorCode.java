package io.github.bsayli.licensing.service.user.operations.errors;

public enum UserOpsErrorCode {
  ALREADY_PROCESSING("user.async.already.processing"),
  MAX_RETRY_ATTEMPTS_EXCEEDED("user.async.max.retry.exceeded");

  private final String messageKey;

  UserOpsErrorCode(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
