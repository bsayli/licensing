package com.c9.licensing.service.user.operations.errors;

public class AlreadyProcessingException extends RuntimeException {

  private static final long serialVersionUID = 7500657643191215045L;

  private final String userId;

  public AlreadyProcessingException(String userId) {
    super("Another asynchronous process is already running for userId: " + userId);
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
