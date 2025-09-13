package io.github.bsayli.licensing.repository.exception;

public class UserAttributeInvalidFormatException extends RepositoryException {

  public UserAttributeInvalidFormatException(String attributeName, String value) {
    super(RepositoryErrorCode.USER_ATTRIBUTE_INVALID_FORMAT, attributeName, value);
  }

  public UserAttributeInvalidFormatException(String attributeName, String value, Throwable cause) {
    super(RepositoryErrorCode.USER_ATTRIBUTE_INVALID_FORMAT, cause, attributeName, value);
  }
}
