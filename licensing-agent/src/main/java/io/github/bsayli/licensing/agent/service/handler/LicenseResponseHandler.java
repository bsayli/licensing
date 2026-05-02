package io.github.bsayli.licensing.agent.service.handler;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.error.ErrorItem;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class LicenseResponseHandler {

    private static final String CODE_REMOTE_ERROR = "REMOTE_ERROR";
    private static final String CODE_EMPTY_TOKEN = "EMPTY_TOKEN";
    private static final String SEP = " : ";

    private static final String KEY_ERROR_CODE = "errorCode";

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
        if (token != null) {
            return token;
        }

        String top = msgOrFallback(KEY_TOP_EMPTY_TOKEN, FALLBACK_TOP_REMOTE_FAILED);
        String detail = msgOrFallback(KEY_DETAIL_EMPTY_TOKEN, FALLBACK_DETAIL_EMPTY_TOKEN);

        throw new LicensingAgentRemoteServiceException(
                FALLBACK_HTTP,
                CODE_EMPTY_TOKEN,
                top,
                List.of(detail));
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
        if (resp == null) {
            return null;
        }

        LicenseAccessResponse data = resp.getData();
        if (data == null) {
            return null;
        }

        String token = data.getLicenseToken();
        return (token == null || token.isBlank()) ? null : token;
    }

    private HttpStatus resolveHttp(ApiProblemException ex) {
        if (ex == null) {
            return FALLBACK_HTTP;
        }

        HttpStatus resolved = HttpStatus.resolve(ex.getStatus());
        return resolved != null ? resolved : FALLBACK_HTTP;
    }

    private String resolveErrorCode(ApiProblemException ex) {
        if (ex == null) {
            return CODE_REMOTE_ERROR;
        }

        String exceptionCode = safeTrim(ex.getErrorCode());
        if (!exceptionCode.isBlank()) {
            return exceptionCode;
        }

        ProblemDetail pd = ex.getProblem();
        if (pd == null) {
            return CODE_REMOTE_ERROR;
        }

        Map<String, Object> properties = pd.getProperties();
        if (properties == null || properties.isEmpty()) {
            return CODE_REMOTE_ERROR;
        }

        Object raw = properties.get(KEY_ERROR_CODE);
        if (raw instanceof String value) {
            String propertyCode = safeTrim(value);
            if (!propertyCode.isBlank()) {
                return propertyCode;
            }
        }

        return CODE_REMOTE_ERROR;
    }

    private List<String> resolveDetails(ApiProblemException ex) {
        String fallback = msgOrFallback(KEY_DETAIL_NO_PAYLOAD, FALLBACK_DETAIL_NO_PAYLOAD);

        if (ex == null) {
            return List.of(fallback);
        }

        if (ex.hasErrors()) {
            List<String> mapped =
                    ex.getErrors().stream()
                            .map(this::formatError)
                            .filter(s -> s != null && !s.isBlank())
                            .toList();

            if (!mapped.isEmpty()) {
                return mapped;
            }
        }

        ProblemDetail pd = ex.getProblem();
        if (pd == null) {
            return List.of(fallback);
        }

        String detail = safeTrim(pd.getDetail());
        if (!detail.isBlank()) {
            return List.of(detail);
        }

        String title = safeTrim(pd.getTitle());
        if (!title.isBlank()) {
            return List.of(title);
        }

        return List.of(fallback);
    }

    private String formatError(ErrorItem error) {
        if (error == null)
            return "";

        String msg = safeTrim(error.message());
        String code = safeTrim(error.code());

        if (!msg.isBlank())
            return msg;

        return code;
    }

    private String msgOrFallback(String key, String fallback) {
        String value = messages.getMessage(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}