package io.github.bsayli.licensing.service.user.operations.errors;

public class AlreadyProcessingException extends UserOpsExceptionImpl {
  public AlreadyProcessingException(String userId) {
    super(UserOpsErrorCode.ALREADY_PROCESSING, userId);
  }

  public AlreadyProcessingException(Throwable cause, String userId) {
    super(UserOpsErrorCode.ALREADY_PROCESSING, cause, userId);
  }
}
