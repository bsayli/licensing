package com.c9.licensing.model.errors.repository;

import com.c9.licensing.model.UserErrorCode;

public interface UserException {
  UserErrorCode getErrorCode();
}
