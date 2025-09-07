package io.github.bsayli.licensing.model.errors;

public abstract class LicenseServiceExceptionImpl extends RuntimeException
    implements LicenseServiceException {

  private final LicenseServiceStatus status;
  private final transient Object[] messageArgs;

  protected LicenseServiceExceptionImpl(LicenseServiceStatus status, Object... messageArgs) {
    super(status.getMessageKey());
    this.status = status;
    this.messageArgs = messageArgs;
  }

  protected LicenseServiceExceptionImpl(
      LicenseServiceStatus status, Throwable cause, Object... messageArgs) {
    super(status.getMessageKey(), cause);
    this.status = status;
    this.messageArgs = messageArgs;
  }

  @Override
  public LicenseServiceStatus getStatus() {
    return status;
  }

  @Override
  public String getMessageKey() {
    return status.getMessageKey();
  }

  @Override
  public Object[] getMessageArgs() {
    return messageArgs;
  }
}
