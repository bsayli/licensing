package com.c9.licensing.errors.repository;

import com.c9.licensing.model.UserErrorCode;

public interface UserException {
    UserErrorCode getErrorCode();
}
