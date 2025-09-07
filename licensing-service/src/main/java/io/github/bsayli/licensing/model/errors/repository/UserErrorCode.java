package io.github.bsayli.licensing.model.errors.repository;

public enum UserErrorCode {
  USER_NOT_FOUND("user.not.found"),
  USER_ATTRIBUTE_MISSING("user.attribute.missing"),
  USER_ATTRIBUTE_INVALID_FORMAT("user.attribute.invalid.format");

  private final String messageKey;

  UserErrorCode(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
