package io.github.bsayli.licensing.sdk.common.exception;


public abstract class ServiceException extends RuntimeException {

  private final ServiceErrorCode code;
  private final transient Object[] args;

  protected ServiceException(ServiceErrorCode code, Object... args) {
    super(code.messageKey());
    this.code = code;
    this.args = args;
  }

  protected ServiceException(ServiceErrorCode code, Throwable cause, Object... args) {
    super(code.messageKey(), cause);
    this.code = code;
    this.args = args;
  }

  public ServiceErrorCode getCode() {
    return code;
  }

  public String getMessageKey() {
    return code.messageKey();
  }

  public Object[] getArgs() {
    return args;
  }
}
