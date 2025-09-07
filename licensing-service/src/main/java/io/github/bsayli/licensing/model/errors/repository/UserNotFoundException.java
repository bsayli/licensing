package io.github.bsayli.licensing.model.errors.repository;

public class UserNotFoundException extends RepositoryExceptionImpl {
  public UserNotFoundException(Object... args) {
    super(UserErrorCode.USER_NOT_FOUND, args);
  }

  public UserNotFoundException(Throwable cause, Object... args) {
    super(UserErrorCode.USER_NOT_FOUND, cause, args);
  }
}
