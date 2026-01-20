package io.github.bsayli.licensing.agent.service.handler;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.client.common.problem.ApiClientException;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.dto.ErrorItem;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LicenseResponseHandler {

    private static final String CODE_REMOTE_ERROR = "REMOTE_ERROR";
    private static final String CODE_EMPTY_TOKEN = "EMPTY_TOKEN";
    private static final String SEP = " : ";

    private static final String KEY_TOP_REMOTE_FAILED = "sdk.remote.call.failed";
    private static final String KEY_TOP_EMPTY_TOKEN = "sdk.remote.empty.token.top";
    private static final String KEY_DETAIL_EMPTY_TOKEN = "sdk.remote.empty.token.detail";
    private static final String KEY_DETAIL_NO_PAYLOAD = "sdk.remote.no.payload";

    private static final String FALLBACK_TOP_REMOTE_FAILED = "Remote call failed";
    private static final String FALLBACK_DETAIL_NO_PAYLOAD = "no-payload";
    private static final String FALLBACK_DETAIL_EMPTY_TOKEN = "empty-token";

    private final LocalizedMessageResolver messages;

    public LicenseResponseHandler(LocalizedMessageResolver messages) {
        this.messages = messages;
    }

    public String extractTokenOrThrow(ServiceResponse<LicenseAccessResponse> resp) {
        String token = safeToken(resp);
        if (token == null) {
            String top = messages.getMessage(KEY_TOP_EMPTY_TOKEN);
            if (top == null || top.isBlank()) top = FALLBACK_TOP_REMOTE_FAILED;

            String d = messages.getMessage(KEY_DETAIL_EMPTY_TOKEN);
            if (d == null || d.isBlank()) d = FALLBACK_DETAIL_EMPTY_TOKEN;

            throw new LicensingSdkRemoteServiceException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    CODE_EMPTY_TOKEN,
                    top,
                    List.of(d));
        }
        return token;
    }

    public String extractTokenIfPresent(ServiceResponse<LicenseAccessResponse> resp) {
        return safeToken(resp);
    }

    public LicensingSdkRemoteServiceException mapRemoteFailure(ApiClientException ex) {
        ApiProblemException pe = ex.getProblem();

        HttpStatus http = resolveHttp(pe);
        String code = resolveErrorCode(pe);

        String top = messages.getMessage(KEY_TOP_REMOTE_FAILED);
        if (top == null || top.isBlank()) {
            top = FALLBACK_TOP_REMOTE_FAILED;
        }

        List<String> details = resolveDetails(pe);

        return new LicensingSdkRemoteServiceException(http, code, top, details);
    }

    private String safeToken(ServiceResponse<LicenseAccessResponse> resp) {
        if (resp == null) return null;
        LicenseAccessResponse data = resp.getData();
        if (data == null) return null;
        String t = data.getLicenseToken();
        return (t == null || t.isBlank()) ? null : t;
    }

    private HttpStatus resolveHttp(ApiProblemException pe) {
        if (pe == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        HttpStatus h = HttpStatus.resolve(pe.getStatus());
        return (h != null) ? h : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveErrorCode(ApiProblemException pe) {
        if (pe == null) return CODE_REMOTE_ERROR;

        String c = pe.getErrorCode();
        if (c != null && !c.isBlank()) return c.trim();

        ProblemDetail pd = pe.getProblem();
        String pdCode = (pd != null) ? pd.getErrorCode() : null;
        if (pdCode != null && !pdCode.isBlank()) return pdCode.trim();

        return CODE_REMOTE_ERROR;
    }

    private List<String> resolveDetails(ApiProblemException pe) {
        String fallback = messages.getMessage(KEY_DETAIL_NO_PAYLOAD);
        if (fallback == null || fallback.isBlank()) {
            fallback = FALLBACK_DETAIL_NO_PAYLOAD;
        }

        if (pe == null) {
            return List.of(fallback);
        }

        if (pe.hasErrors()) {
            List<String> errors =
                    pe.getErrors().stream()
                            .map(this::formatError)
                            .filter(s -> !s.isBlank())
                            .toList();

            return errors.isEmpty() ? List.of(fallback) : errors;
        }

        ProblemDetail pd = pe.getProblem();
        if (pd == null) {
            return List.of(fallback);
        }

        if (pd.getDetail() != null && !pd.getDetail().isBlank()) {
            return List.of(pd.getDetail().trim());
        }

        if (pd.getTitle() != null && !pd.getTitle().isBlank()) {
            return List.of(pd.getTitle().trim());
        }

        return List.of(fallback);
    }

    private String formatError(ErrorItem e) {
        if (e == null) return "";
        String code = safe(e.getCode());
        String msg = safe(e.getMessage());
        if (code.isBlank()) return msg;
        if (msg.isBlank()) return code;
        return code + SEP + msg;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}