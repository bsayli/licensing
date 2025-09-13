package io.github.bsayli.licensing.sdk.service.handler;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.sdk.common.i18n.LocalizedMessageResolver;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LicenseResponseHandler {

  private static final int HTTP_OK = HttpStatus.OK.value();
  private static final String CODE_REMOTE_ERROR = "REMOTE_ERROR";
  private static final String CODE_EMPTY_TOKEN = "EMPTY_TOKEN";
  private static final String SEP = " : ";

  private static final String KEY_TOP_REMOTE_FAILED = "sdk.remote.call.failed";
  private static final String KEY_TOP_EMPTY_TOKEN = "sdk.remote.empty.token.top";
  private static final String KEY_DETAIL_EMPTY_TOKEN = "sdk.remote.empty.token.detail";
  private static final String KEY_DETAIL_NO_PAYLOAD = "sdk.remote.no.payload";

  private final LocalizedMessageResolver messages;

  public LicenseResponseHandler(LocalizedMessageResolver messages) {
    this.messages = messages;
  }

  private <T> void requireOk(ApiClientResponse<T> resp) {
    if (isOk(resp)) return;
    throw buildRemoteException(resp);
  }

  private <T> boolean isOk(ApiClientResponse<T> resp) {
    return resp != null && resp.getStatus() != null && resp.getStatus() == HTTP_OK;
  }

  public String extractTokenOrThrow(ApiClientResponse<LicenseAccessResponse> resp) {
    requireOk(resp);
    String token = safeToken(resp);
    if (token == null) {
      throw new LicensingSdkRemoteServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          CODE_EMPTY_TOKEN,
          messages.getMessage(KEY_TOP_EMPTY_TOKEN),
          List.of(messages.getMessage(KEY_DETAIL_EMPTY_TOKEN)));
    }
    return token;
  }

  public String extractTokenIfPresentOrThrow(ApiClientResponse<LicenseAccessResponse> resp) {
    requireOk(resp);
    return safeToken(resp);
  }

  private String safeToken(ApiClientResponse<LicenseAccessResponse> resp) {
    var data = resp.getData();
    if (data == null) return null;
    String t = data.getLicenseToken();
    return (t == null || t.isBlank()) ? null : t;
  }

  private <T> LicensingSdkRemoteServiceException buildRemoteException(ApiClientResponse<T> resp) {
    HttpStatus status = resolveStatus(resp);
    String top = resolveTopMessage(resp);
    String errorCode = resolveErrorCode(resp);
    List<String> details = resolveDetails(resp);
    return new LicensingSdkRemoteServiceException(status, errorCode, top, details);
  }

  private <T> HttpStatus resolveStatus(ApiClientResponse<T> resp) {
    if (resp == null || resp.getStatus() == null) return HttpStatus.INTERNAL_SERVER_ERROR;
    return HttpStatus.valueOf(resp.getStatus());
  }

  private <T> String resolveTopMessage(ApiClientResponse<T> resp) {
    if (resp == null) return messages.getMessage(KEY_TOP_REMOTE_FAILED);
    String msg = resp.getMessage();
    return (msg == null || msg.isBlank()) ? messages.getMessage(KEY_TOP_REMOTE_FAILED) : msg;
  }

  private <T> String resolveErrorCode(ApiClientResponse<T> resp) {
    if (resp == null || resp.getErrors() == null || resp.getErrors().isEmpty()) {
      return CODE_REMOTE_ERROR;
    }
    var first = resp.getErrors().getFirst();
    String code = first.errorCode();
    return (code == null || code.isBlank()) ? CODE_REMOTE_ERROR : code;
  }

  private <T> List<String> resolveDetails(ApiClientResponse<T> resp) {
    if (resp == null || resp.getErrors() == null || resp.getErrors().isEmpty()) {
      return List.of(messages.getMessage(KEY_DETAIL_NO_PAYLOAD));
    }
    return resp.getErrors().stream()
        .map(e -> safe(e.errorCode()) + SEP + safe(e.message()))
        .toList();
  }

  private String safe(String s) {
    return (s == null) ? "" : s;
  }
}
