package com.c9.licensing.sdk.exception;

import com.c9.licensing.sdk.model.server.LicenseServerValidationResponse;

public class LicenseServiceClientErrorException extends RuntimeException {

  private static final long serialVersionUID = -8396350239975252530L;

  private final LicenseServerValidationResponse serverResponse;

  public LicenseServiceClientErrorException(
      LicenseServerValidationResponse serverResponse, String message) {
    super(message);
    this.serverResponse = serverResponse;
  }

  public LicenseServerValidationResponse getServerResponse() {
    return serverResponse;
  }
}
