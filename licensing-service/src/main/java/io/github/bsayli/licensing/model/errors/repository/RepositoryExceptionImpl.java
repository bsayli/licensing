package io.github.bsayli.licensing.model.errors.repository;

public abstract class RepositoryExceptionImpl extends RuntimeException implements UserException {

  private final UserErrorCode errorCode;
  private final transient Object[] messageArgs;

  protected RepositoryExceptionImpl(UserErrorCode errorCode, Object... messageArgs) {
    super(errorCode.getMessageKey());
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  protected RepositoryExceptionImpl(
      UserErrorCode errorCode, Throwable cause, Object... messageArgs) {
    super(errorCode.getMessageKey(), cause);
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  @Override
  public UserErrorCode getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessageKey() {
    return errorCode.getMessageKey();
  }

  @Override
  public Object[] getMessageArgs() {
    return messageArgs;
  }
}
