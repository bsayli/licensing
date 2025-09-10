package io.github.bsayli.licensing.client.common.exception;

import org.springframework.http.HttpStatusCode;

public class ApiClientException extends RuntimeException {
  private final HttpStatusCode statusCode;
  private final String responseBody;
  private final String messageKey;

  public ApiClientException(
      String message, HttpStatusCode statusCode, String responseBody, String messageKey) {
    super(message);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
    this.messageKey = messageKey;
  }

  public ApiClientException(
      String message,
      HttpStatusCode statusCode,
      String responseBody,
      String messageKey,
      Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
    this.messageKey = messageKey;
  }

  public HttpStatusCode getStatusCode() {
    return statusCode;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
