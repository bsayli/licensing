package io.github.bsayli.licensing.sdk.exception;

import io.github.bsayli.licensing.sdk.model.server.LicenseServerValidationResponse;

public class LicenseServiceServerErrorException extends RuntimeException {

  private static final long serialVersionUID = -8396350239975252530L;

  private final LicenseServerValidationResponse serverResponse;

  public LicenseServiceServerErrorException(
      LicenseServerValidationResponse serverResponse, String message) {
    super(message);
    this.serverResponse = serverResponse;
  }

  public LicenseServerValidationResponse getServerResponse() {
    return serverResponse;
  }
}
