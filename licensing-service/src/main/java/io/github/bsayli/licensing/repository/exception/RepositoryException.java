package io.github.bsayli.licensing.repository.exception;

public abstract class RepositoryException extends RuntimeException {

  private final RepositoryErrorCode errorCode;
  private final transient Object[] messageArgs;

  protected RepositoryException(RepositoryErrorCode errorCode, Object... messageArgs) {
    super(errorCode.getMessageKey());
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  protected RepositoryException(
      RepositoryErrorCode errorCode, Throwable cause, Object... messageArgs) {
    super(errorCode.getMessageKey(), cause);
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  public RepositoryErrorCode getErrorCode() {
    return errorCode;
  }

  public String getMessageKey() {
    return errorCode.getMessageKey();
  }

  public Object[] getMessageArgs() {
    return messageArgs;
  }
}
