package io.github.bsayli.licensing.sdk.common.exception;

import org.springframework.http.HttpStatusCode;

public class SdkTransportException extends RuntimeException {
  private final HttpStatusCode statusCode;
  private final String messageKey;
  private final String rawBody;

  public SdkTransportException(
      String message,
      HttpStatusCode statusCode,
      String messageKey,
      String rawBody,
      Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.messageKey = messageKey;
    this.rawBody = rawBody;
  }

  public HttpStatusCode getStatusCode() {
    return statusCode;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public String getRawBody() {
    return rawBody;
  }
}
