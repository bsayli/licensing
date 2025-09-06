package io.github.bsayli.licensing.model.errors.repository;

import io.github.bsayli.licensing.model.UserErrorCode;

public interface UserException {
  UserErrorCode getErrorCode();
}
