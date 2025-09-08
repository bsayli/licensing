package io.github.bsayli.licensing.repository.exception;

public class UserAttributeMissingException extends RepositoryException {

  public UserAttributeMissingException(String attributeName) {
    super(RepositoryErrorCode.USER_ATTRIBUTE_MISSING, attributeName);
  }

  public UserAttributeMissingException(Throwable cause, String attributeName) {
    super(RepositoryErrorCode.USER_ATTRIBUTE_MISSING, cause, attributeName);
  }
}
