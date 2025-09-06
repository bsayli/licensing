package io.github.bsayli.licensing.service.user.operations.errors;

public class MaxRetryAttemptsExceededException extends RuntimeException {

  private static final long serialVersionUID = 1803124881133766389L;

  private final String userId;

  public MaxRetryAttemptsExceededException(String userId) {
    super("Retry attempts exceeded for userId : " + userId);
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
