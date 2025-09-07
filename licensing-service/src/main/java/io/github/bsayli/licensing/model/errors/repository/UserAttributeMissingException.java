package io.github.bsayli.licensing.model.errors.repository;

public class UserAttributeMissingException extends RepositoryExceptionImpl {

  public UserAttributeMissingException(String attributeName) {
    super(UserErrorCode.USER_ATTRIBUTE_MISSING, attributeName);
  }

  public UserAttributeMissingException(Throwable cause, String attributeName) {
    super(UserErrorCode.USER_ATTRIBUTE_MISSING, cause, attributeName);
  }
}
