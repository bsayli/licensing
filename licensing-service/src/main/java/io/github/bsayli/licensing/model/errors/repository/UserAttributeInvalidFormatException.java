package io.github.bsayli.licensing.model.errors.repository;

public class UserAttributeInvalidFormatException extends RepositoryExceptionImpl {

  public UserAttributeInvalidFormatException(String attributeName, String value) {
    super(UserErrorCode.USER_ATTRIBUTE_INVALID_FORMAT, attributeName, value);
  }

  public UserAttributeInvalidFormatException(String attributeName, String value, Throwable cause) {
    super(UserErrorCode.USER_ATTRIBUTE_INVALID_FORMAT, cause, attributeName, value);
  }
}
