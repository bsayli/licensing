package io.github.bsayli.licensing.model.errors.repository;

public interface UserException {
  UserErrorCode getErrorCode();

  String getMessageKey();

  Object[] getMessageArgs();
}
