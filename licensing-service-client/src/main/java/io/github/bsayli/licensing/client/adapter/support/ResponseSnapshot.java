package io.github.bsayli.licensing.client.adapter.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("java:S6218")
record ResponseSnapshot(HttpStatusCode status, boolean statusUnavailable, MediaType contentType, byte[] body) {

    private static final Logger log = LoggerFactory.getLogger(ResponseSnapshot.class);

    private static final int MAX_BODY_BYTES = 200_000;

    static ResponseSnapshot read(ClientHttpResponse response) {
        MediaType contentType = response.getHeaders().getContentType();
        StatusRead statusRead = readStatus(response);
        byte[] body = readBody(response);
        return new ResponseSnapshot(statusRead.status, statusRead.unavailable, contentType, body);
    }

    static String preview(byte[] bytes, int maxChars) {
        int len = Math.clamp(maxChars, 0, bytes.length);
        if (len == 0) return "";
        String s = new String(bytes, 0, len, java.nio.charset.StandardCharsets.UTF_8);
        return s.replace("\r", "").replace("\n", " ").trim();
    }

    private static StatusRead readStatus(ClientHttpResponse response) {
        try {
            return new StatusRead(response.getStatusCode(), false);
        } catch (IOException e) {
            log.warn("Unable to read status code from response", e);
            return new StatusRead(HttpStatusCode.valueOf(500), true);
        }
    }

    private static byte[] readBody(ClientHttpResponse response) {
        try (InputStream is = response.getBody()) {
            return is.readNBytes(MAX_BODY_BYTES);
        } catch (IOException e) {
            log.warn("Unable to read response body", e);
            return new byte[0];
        }
    }

    private record StatusRead(HttpStatusCode status, boolean unavailable) {
    }
}