package io.github.bsayli.licensing.service.user.operations.errors;

public interface UserOpsException {
  UserOpsErrorCode getErrorCode();

  String getMessageKey();

  Object[] getMessageArgs();
}
