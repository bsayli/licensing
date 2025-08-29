package com.c9.licensing.sdk.exception;

import com.c9.licensing.sdk.model.server.LicenseServerServiceStatus;

public class TokenAlreadyExistException extends RuntimeException {

  private static final long serialVersionUID = -5338137855230379453L;

  public TokenAlreadyExistException(String message) {
    super(message);
  }

  public LicenseServerServiceStatus getStatus() {
    return LicenseServerServiceStatus.TOKEN_ALREADY_EXIST;
  }
}
