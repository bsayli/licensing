package io.github.bsayli.licensing.agent.cli.model;

import io.github.blueprintplatform.openapi.generics.contract.error.ErrorItem;
import java.util.List;

/**
 * Public error response contract exposed by licensing-agent. This is a framework-agnostic error
 * envelope.
 */
public record LicenseAgentErrorResponse(String errorCode, String message, List<ErrorItem> errors) {

  public static LicenseAgentErrorResponse of(String errorCode, String message) {
    return new LicenseAgentErrorResponse(errorCode, message, null);
  }

  public static LicenseAgentErrorResponse of(
      String errorCode, String message, List<ErrorItem> errors) {
    return new LicenseAgentErrorResponse(errorCode, message, errors);
  }
}
