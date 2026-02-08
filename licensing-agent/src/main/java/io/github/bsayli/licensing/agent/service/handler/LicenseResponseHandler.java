package io.github.bsayli.licensing.agent.service.handler;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
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

    private static final String KEY_TOP_REMOTE_FAILED = "agent.remote.call.failed";
    private static final String KEY_TOP_EMPTY_TOKEN = "agent.remote.empty.token.top";
    private static final String KEY_DETAIL_EMPTY_TOKEN = "agent.remote.empty.token.detail";
    private static final String KEY_DETAIL_NO_PAYLOAD = "agent.remote.no.payload";

    private static final String FALLBACK_TOP_REMOTE_FAILED = "Remote call failed";
    private static final String FALLBACK_DETAIL_NO_PAYLOAD = "no-payload";
    private static final String FALLBACK_DETAIL_EMPTY_TOKEN = "empty-token";

    private static final HttpStatus FALLBACK_HTTP = HttpStatus.INTERNAL_SERVER_ERROR;

    private final LocalizedMessageResolver messages;

    public LicenseResponseHandler(LocalizedMessageResolver messages) {
        this.messages = messages;
    }

    public String extractTokenOrThrow(ServiceResponse<LicenseAccessResponse> resp) {
        String token = safeToken(resp);
        if (token != null) return token;

        String top = msgOrFallback(KEY_TOP_EMPTY_TOKEN, FALLBACK_TOP_REMOTE_FAILED);
        String d = msgOrFallback(KEY_DETAIL_EMPTY_TOKEN, FALLBACK_DETAIL_EMPTY_TOKEN);

        throw new LicensingAgentRemoteServiceException(
                FALLBACK_HTTP,
                CODE_EMPTY_TOKEN,
                top,
                List.of(d));
    }

    public String extractTokenIfPresent(ServiceResponse<LicenseAccessResponse> resp) {
        return safeToken(resp);
    }

    public LicensingAgentRemoteServiceException mapRemoteFailure(ApiProblemException ex) {
        HttpStatus http = resolveHttp(ex);
        String code = resolveErrorCode(ex);
        String top = msgOrFallback(KEY_TOP_REMOTE_FAILED, FALLBACK_TOP_REMOTE_FAILED);
        List<String> details = resolveDetails(ex);

        return new LicensingAgentRemoteServiceException(http, code, top, details);
    }

    private String safeToken(ServiceResponse<LicenseAccessResponse> resp) {
        if (resp == null) return null;
        LicenseAccessResponse data = resp.getData();
        if (data == null) return null;
        String t = data.getLicenseToken();
        return (t == null || t.isBlank()) ? null : t;
    }

    private HttpStatus resolveHttp(ApiProblemException ex) {
        if (ex == null) return FALLBACK_HTTP;
        HttpStatus h = HttpStatus.resolve(ex.getStatus());
        return (h != null) ? h : FALLBACK_HTTP;
    }

    private String resolveErrorCode(ApiProblemException ex) {
        if (ex == null) return CODE_REMOTE_ERROR;

        String c = ex.getErrorCode();
        if (c != null && !c.isBlank()) return c.trim();

        ProblemDetail pd = ex.getProblem();
        String pdCode = (pd != null) ? pd.getErrorCode() : null;
        if (pdCode != null && !pdCode.isBlank()) return pdCode.trim();

        return CODE_REMOTE_ERROR;
    }

    private List<String> resolveDetails(ApiProblemException ex) {
        String fallback = msgOrFallback(KEY_DETAIL_NO_PAYLOAD, FALLBACK_DETAIL_NO_PAYLOAD);

        if (ex == null) return List.of(fallback);

        if (ex.hasErrors()) {
            List<String> errors =
                    ex.getErrors().stream()
                            .map(this::formatError)
                            .filter(s -> s != null && !s.isBlank())
                            .toList();
            return errors.isEmpty() ? List.of(fallback) : errors;
        }

        ProblemDetail pd = ex.getProblem();
        if (pd == null) return List.of(fallback);

        String d = pd.getDetail();
        if (d != null && !d.isBlank()) return List.of(d.trim());

        String t = pd.getTitle();
        if (t != null && !t.isBlank()) return List.of(t.trim());

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

    private String msgOrFallback(String key, String fallback) {
        String s = messages.getMessage(key);
        if (s == null || s.isBlank()) return fallback;
        return s;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}