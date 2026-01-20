package io.github.bsayli.licensing.client.adapter.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.client.generated.dto.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

public final class ProblemDetailSupport {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailSupport.class);

    private static final String PROP_PREVIEW_ENABLED = "licensing.client.problem.preview.enabled";
    private static final String PROP_PREVIEW_MAX_CHARS = "licensing.client.problem.preview.max-chars";
    private static final int DEFAULT_PREVIEW_MAX_CHARS = 0;

    private ProblemDetailSupport() {
    }

    public static ProblemDetail extract(ObjectMapper om, ClientHttpResponse response) {
        ResponseSnapshot snap = ResponseSnapshot.read(response);

        if (snap.body().length == 0) {
            return snap.statusUnavailable()
                    ? ProblemDetailFallbacks.statusUnavailable(snap.contentType())
                    : ProblemDetailFallbacks.emptyBody(snap.status(), snap.contentType());
        }

        if (!isJson(snap.contentType())) {
            String preview = previewOrEmpty(snap.body());
            logNonJson(snap, preview);
            return ProblemDetailFallbacks.nonJson(snap.status(), snap.contentType(), preview, snap.statusUnavailable());
        }

        try {
            return om.readValue(snap.body(), ProblemDetail.class);
        } catch (Exception e) {
            String preview = previewOrEmpty(snap.body());
            logUnparsable(snap, preview, e);
            return ProblemDetailFallbacks.unparsable(snap.status(), snap.contentType(), preview, snap.statusUnavailable());
        }
    }

    private static boolean isJson(MediaType contentType) {
        if (contentType == null) return false;
        return MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
                || MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(contentType);
    }

    private static void logNonJson(ResponseSnapshot snap, String preview) {
        if (previewDisabled()) {
            log.warn("Upstream returned non-JSON error response (status={}, contentType={})", snap.status(), snap.contentType());
            return;
        }
        log.warn(
                "Upstream returned non-JSON error response (status={}, contentType={}, preview={})",
                snap.status(),
                snap.contentType(),
                preview);
    }

    private static void logUnparsable(ResponseSnapshot snap, String preview, Exception e) {
        if (previewDisabled()) {
            log.warn("Unable to deserialize ProblemDetail (status={}, contentType={})", snap.status(), snap.contentType(), e);
            return;
        }
        log.warn(
                "Unable to deserialize ProblemDetail (status={}, contentType={}, preview={})",
                snap.status(),
                snap.contentType(),
                preview,
                e);
    }

    private static boolean previewDisabled() {
        return !Boolean.parseBoolean(System.getProperty(PROP_PREVIEW_ENABLED, "false"));
    }

    private static int previewMaxChars() {
        String v = System.getProperty(PROP_PREVIEW_MAX_CHARS, String.valueOf(DEFAULT_PREVIEW_MAX_CHARS));
        try {
            return Math.max(0, Integer.parseInt(v));
        } catch (Exception ignored) {
            return DEFAULT_PREVIEW_MAX_CHARS;
        }
    }

    private static String previewOrEmpty(byte[] body) {
        int max = previewMaxChars();
        if (previewDisabled() || max <= 0 || body == null || body.length == 0) return "";
        return ResponseSnapshot.preview(body, max);
    }
}