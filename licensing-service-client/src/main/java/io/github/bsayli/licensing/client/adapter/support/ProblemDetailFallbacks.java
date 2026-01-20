package io.github.bsayli.licensing.client.adapter.support;

import io.github.bsayli.licensing.client.generated.dto.ErrorItem;
import io.github.bsayli.licensing.client.generated.dto.ProblemDetail;
import io.github.bsayli.licensing.client.generated.dto.ProblemDetailExtensions;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

import java.net.URI;

final class ProblemDetailFallbacks {

    private static final HttpStatusCode STATUS_INTERNAL_SERVER_ERROR = HttpStatusCode.valueOf(500);

    private static final String TITLE_HTTP_ERROR = "HTTP error";
    private static final String TITLE_NON_JSON = "Non-JSON error response";
    private static final String TITLE_UNPARSABLE = "Unparseable problem response";
    private static final String TITLE_EMPTY = "Empty problem response body";

    private static final String DETAIL_NON_JSON = "Upstream returned non-JSON error response.";
    private static final String DETAIL_UNPARSABLE = "Upstream returned a problem response, but it could not be parsed.";
    private static final String DETAIL_EMPTY = "Upstream returned an empty error response body.";
    private static final String DETAIL_STATUS_UNAVAILABLE = "Unable to read HTTP status from upstream.";

    private static final String ERROR_CODE_UPSTREAM_NON_JSON = "UPSTREAM_NON_JSON_ERROR";
    private static final String ERROR_CODE_UPSTREAM_UNPARSABLE = "UPSTREAM_UNPARSABLE_PROBLEM";
    private static final String ERROR_CODE_UPSTREAM_EMPTY = "UPSTREAM_EMPTY_PROBLEM";
    private static final String ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE = "UPSTREAM_STATUS_UNAVAILABLE";

    private static final URI TYPE_NON_JSON = URI.create("https://example.com/problems/upstream-non-json");
    private static final URI TYPE_UNPARSABLE = URI.create("https://example.com/problems/upstream-unparseable");
    private static final URI TYPE_EMPTY = URI.create("https://example.com/problems/upstream-empty");
    private static final URI TYPE_STATUS_UNAVAILABLE = URI.create("https://example.com/problems/upstream-status-unavailable");

    private static final String ERROR_ITEM_RESOURCE_UPSTREAM = "upstream";
    private static final String ERROR_ITEM_FIELD_CONTENT_TYPE = "contentType";
    private static final String ERROR_ITEM_FIELD_BODY_PREVIEW = "bodyPreview";
    private static final String ERROR_ITEM_FIELD_STATUS = "status";

    private static final String MSG_CONTENT_TYPE_PREFIX = "Upstream Content-Type: ";
    private static final String MSG_STATUS_UNAVAILABLE = "unavailable";

    private ProblemDetailFallbacks() {
    }

    static ProblemDetail emptyBody(HttpStatusCode status, MediaType contentType) {
        ProblemDetail pd =
                baseProblem(status, TYPE_EMPTY, TITLE_EMPTY, DETAIL_EMPTY, ERROR_CODE_UPSTREAM_EMPTY);
        addContextErrors(pd, ERROR_CODE_UPSTREAM_EMPTY, false, contentType, "");
        return pd;
    }

    static ProblemDetail statusUnavailable(MediaType contentType) {
        ProblemDetail pd =
                baseProblem(
                        STATUS_INTERNAL_SERVER_ERROR,
                        TYPE_STATUS_UNAVAILABLE,
                        TITLE_HTTP_ERROR,
                        DETAIL_STATUS_UNAVAILABLE,
                        ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE);
        addContextErrors(pd, ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE, true, contentType, "");
        return pd;
    }

    static ProblemDetail nonJson(
            HttpStatusCode status, MediaType contentType, String preview, boolean statusUnavailable) {
        ProblemDetail pd =
                baseProblem(status, TYPE_NON_JSON, TITLE_NON_JSON, DETAIL_NON_JSON, ERROR_CODE_UPSTREAM_NON_JSON);
        addContextErrors(pd, ERROR_CODE_UPSTREAM_NON_JSON, statusUnavailable, contentType, preview);
        return pd;
    }

    static ProblemDetail unparsable(
            HttpStatusCode status, MediaType contentType, String preview, boolean statusUnavailable) {
        ProblemDetail pd =
                baseProblem(
                        status,
                        TYPE_UNPARSABLE,
                        TITLE_UNPARSABLE,
                        DETAIL_UNPARSABLE,
                        ERROR_CODE_UPSTREAM_UNPARSABLE);
        addContextErrors(pd, ERROR_CODE_UPSTREAM_UNPARSABLE, statusUnavailable, contentType, preview);
        return pd;
    }

    private static ProblemDetail baseProblem(
            HttpStatusCode status, URI type, String title, String detail, String errorCode) {

        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(status.value());
        pd.setType(type);
        pd.setTitle((title != null && !title.isBlank()) ? title : TITLE_HTTP_ERROR);
        pd.setDetail(detail);
        pd.setErrorCode(errorCode);
        return pd;
    }

    private static void addContextErrors(
            ProblemDetail pd, String problemCode, boolean statusUnavailable, MediaType contentType, String preview) {

        ProblemDetailExtensions ext = new ProblemDetailExtensions();

        String ct = contentType != null ? contentType.toString() : "";
        if (!ct.isBlank()) {
            ext.addErrorsItem(
                    errorItem(problemCode, MSG_CONTENT_TYPE_PREFIX + ct, ERROR_ITEM_FIELD_CONTENT_TYPE));
        }

        if (statusUnavailable) {
            ext.addErrorsItem(
                    errorItem(
                            ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE,
                            MSG_STATUS_UNAVAILABLE,
                            ERROR_ITEM_FIELD_STATUS));
        }

        if (preview != null && !preview.isBlank()) {
            ext.addErrorsItem(errorItem(problemCode, preview, ERROR_ITEM_FIELD_BODY_PREVIEW));
        }

        if (ext.getErrors() != null && !ext.getErrors().isEmpty()) {
            pd.setExtensions(ext);
        }
    }

    private static ErrorItem errorItem(String code, String message, String field) {
        ErrorItem item = new ErrorItem();
        item.setCode(code);
        item.setMessage(message);
        item.setField(field);
        item.setResource(ERROR_ITEM_RESOURCE_UPSTREAM);
        return item;
    }
}