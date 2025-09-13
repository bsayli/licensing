package io.github.bsayli.licensing.repository.exception;

public class UserNotFoundException extends RepositoryException {
  public UserNotFoundException(Object... args) {
    super(RepositoryErrorCode.USER_NOT_FOUND, args);
  }

  public UserNotFoundException(Throwable cause, Object... args) {
    super(RepositoryErrorCode.USER_NOT_FOUND, cause, args);
  }
}
