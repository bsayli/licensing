package io.github.bsayli.licensing.service.user.operations.errors;

public abstract class UserOpsExceptionImpl extends RuntimeException implements UserOpsException {

  private final UserOpsErrorCode errorCode;
  private final String messageKey;
  private final transient Object[] messageArgs;

  protected UserOpsExceptionImpl(UserOpsErrorCode errorCode, Object... messageArgs) {
    super(errorCode.getMessageKey());
    this.errorCode = errorCode;
    this.messageKey = errorCode.getMessageKey();
    this.messageArgs = messageArgs;
  }

  protected UserOpsExceptionImpl(
      UserOpsErrorCode errorCode, Throwable cause, Object... messageArgs) {
    super(errorCode.getMessageKey(), cause);
    this.errorCode = errorCode;
    this.messageKey = errorCode.getMessageKey();
    this.messageArgs = messageArgs;
  }

  @Override
  public UserOpsErrorCode getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessageKey() {
    return messageKey;
  }

  @Override
  public Object[] getMessageArgs() {
    return messageArgs;
  }
}
