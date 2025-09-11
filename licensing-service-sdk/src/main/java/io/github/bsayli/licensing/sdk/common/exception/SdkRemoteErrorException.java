package io.github.bsayli.licensing.sdk.common.exception;

import io.github.bsayli.licensing.sdk.common.api.ApiError;
import java.util.List;
import org.springframework.http.HttpStatus;

public class SdkRemoteErrorException extends RuntimeException {
  private final HttpStatus status;
  private final String topMessage;
  private final List<ApiError> errors;

  public SdkRemoteErrorException(HttpStatus status, String topMessage, List<ApiError> errors) {
    super(topMessage);
    this.status = status;
    this.topMessage = topMessage;
    this.errors = errors;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getTopMessage() {
    return topMessage;
  }

  public List<ApiError> getErrors() {
    return errors;
  }
}
