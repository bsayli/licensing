package io.github.bsayli.licensing.service.user.exception;

public abstract class UserOperationException extends RuntimeException {

  private final UserOperationErrorCode errorCode;
  private final transient Object[] messageArgs;

  protected UserOperationException(UserOperationErrorCode errorCode, Object... messageArgs) {
    super(errorCode.messageKey());
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  protected UserOperationException(
      UserOperationErrorCode errorCode, Throwable cause, Object... messageArgs) {
    super(errorCode.messageKey(), cause);
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  public UserOperationErrorCode getErrorCode() {
    return errorCode;
  }

  public String getMessageKey() {
    return errorCode.messageKey();
  }

  public Object[] getMessageArgs() {
    return messageArgs;
  }
}
