package io.github.bsayli.licensing.service.user.operations.exception;

public class MaxRetryAttemptsExceededException extends UserOperationException {
  public MaxRetryAttemptsExceededException(String userId) {
    super(UserOperationErrorCode.MAX_RETRY_ATTEMPTS_EXCEEDED, userId);
  }

  public MaxRetryAttemptsExceededException(Throwable cause, String userId) {
    super(UserOperationErrorCode.MAX_RETRY_ATTEMPTS_EXCEEDED, cause, userId);
  }
}
