package io.github.bsayli.licensing.service.user.operations.exception;

public class AlreadyProcessingException extends UserOperationException {
  public AlreadyProcessingException(String userId) {
    super(UserOperationErrorCode.ALREADY_PROCESSING, userId);
  }

  public AlreadyProcessingException(Throwable cause, String userId) {
    super(UserOperationErrorCode.ALREADY_PROCESSING, cause, userId);
  }
}
