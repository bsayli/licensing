package io.github.bsayli.licensing.service.user.operations.errors;

public class MaxRetryAttemptsExceededException extends UserOpsExceptionImpl {
  public MaxRetryAttemptsExceededException(String userId) {
    super(UserOpsErrorCode.MAX_RETRY_ATTEMPTS_EXCEEDED, userId);
  }

  public MaxRetryAttemptsExceededException(Throwable cause, String userId) {
    super(UserOpsErrorCode.MAX_RETRY_ATTEMPTS_EXCEEDED, cause, userId);
  }
}
